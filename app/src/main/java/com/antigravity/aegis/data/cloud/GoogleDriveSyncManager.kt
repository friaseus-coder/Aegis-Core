package com.antigravity.aegis.data.cloud

import android.content.Context
import android.net.Uri
import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona el almacenamiento de archivos y backups en Google Drive.
 */
@Singleton
class GoogleDriveSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleAppsManager: GoogleAppsManager
) {
    private val folderName = "Aegis_Backups"

    /**
     * Obtiene o crea la carpeta Aegis_Backups en la raíz de Drive del usuario.
     */
    private suspend fun getOrCreateFolder(service: Drive): String = withContext(Dispatchers.IO) {
        val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val result: FileList = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val folder = result.files.firstOrNull()
        if (folder != null) {
            return@withContext folder.id
        }

        val folderMetadata = File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
        }

        val createdFolder = service.files().create(folderMetadata)
            .setFields("id")
            .execute()

        return@withContext createdFolder.id
    }

    /**
     * Sube un archivo a la carpeta Aegis_Backups.
     * Si el archivo con el mismo nombre ya existe, lo actualiza.
     */
    suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        inputStream: InputStream
    ): String? = withContext(Dispatchers.IO) {
        val account = googleAppsManager.getLastSignedInAccount() ?: return@withContext null
        val service = googleAppsManager.getDriveService(account)
        val folderId = getOrCreateFolder(service)

        try {
            // Verificar si existe para actualizar
            val query = "name = '$fileName' and '$folderId' in parents and trashed = false"
            val existingFiles = service.files().list().setQ(query).setFields("files(id)").execute()
            val existingFile = existingFiles.files.firstOrNull()

            val metadata = File().apply {
                name = fileName
                parents = listOf(folderId)
            }
            
            val mediaContent = InputStreamContent(mimeType, inputStream)

            if (existingFile != null) {
                // Actualizar
                val updated = service.files().update(existingFile.id, null, mediaContent).execute()
                return@withContext updated.id
            } else {
                // Crear
                val created = service.files().create(metadata, mediaContent)
                    .setFields("id")
                    .execute()
                return@withContext created.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Sube una copia de seguridad de la base de datos actual.
     */
    suspend fun uploadDatabaseBackup(): String? = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("aegis_core.db")
        if (!dbFile.exists()) return@withContext null

        FileInputStream(dbFile).use { input ->
            return@withContext uploadFile(
                fileName = "aegis_core_backup.db",
                mimeType = "application/x-sqlite3",
                inputStream = input
            )
        }
    }

    /**
     * Sube un archivo adjunto desde una URI local.
     */
    suspend fun uploadAttachment(uri: Uri, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            
            inputStream.use { input ->
                return@withContext uploadFile(fileName, mimeType, input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
