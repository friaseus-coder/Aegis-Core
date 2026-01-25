package com.antigravity.aegis.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.antigravity.aegis.data.security.FileEncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileEncryptionManager: FileEncryptionManager
) {

    /**
     * Guarda un archivo en el almacenamiento interno encriptado.
     * Retorna el nombre del archivo guardado.
     */
    suspend fun saveAttachmentEncrypted(fileName: String, inputStream: InputStream): String = withContext(Dispatchers.IO) {
        val file = fileEncryptionManager.getFile(fileName)
        // Si el archivo ya existe, se sobrescribe
        if (file.exists()) {
            file.delete()
        }
        
        val outputStream = fileEncryptionManager.getEncryptedOutputStream(file)
        
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        return@withContext fileName
    }

    /**
     * Lee un archivo encriptado y retorna un InputStream.
     */
    fun getAttachmentInputStream(fileName: String): InputStream {
        val file = fileEncryptionManager.getFile(fileName)
        return fileEncryptionManager.getEncryptedInputStream(file)
    }

    /**
     * Exporta un archivo (interno/encriptado) a una URI externa (ej: Google Drive via SAF).
     * El archivo se desencripta al vuelo mientras se escribe en el destino.
     */
    suspend fun exportAttachmentToUri(fileName: String, destinationUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = getAttachmentInputStream(fileName)
            val outputStream = context.contentResolver.openOutputStream(destinationUri) 
                ?: return@withContext false

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Elimina un archivo adjunto.
     */
    suspend fun deleteAttachment(fileName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext fileEncryptionManager.deleteFile(fileName)
    }
}
