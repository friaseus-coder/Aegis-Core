package com.antigravity.aegis.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.EntryPointAccessors

class AegisDatabaseCallback(
    private val context: Context,
    private val importTemplateUseCaseProvider: javax.inject.Provider<com.antigravity.aegis.domain.usecase.project.ImportTemplateUseCase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Lanzamos una corutina en segundo plano para leer los JSON y precargarlos
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Buscamos los archivos en la carpeta de templates
                val templatesDir = "templates"
                val files = context.assets.list(templatesDir)
                
                if (files != null) {
                    val importUseCase = importTemplateUseCaseProvider.get()
                    
                    for (fileName in files) {
                        if (fileName.endsWith(".json")) {
                            val inputStream = context.assets.open("$templatesDir/$fileName")
                            val jsonString = inputStream.bufferedReader().use { it.readText() }
                            
                            // Insertamos la plantilla parseada en la base de datos
                            importUseCase.invokeFromJson(jsonString)
                            android.util.Log.d("AegisDatabase", "Precargada plantilla base: $fileName")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AegisDatabase", "Error precargando plantillas base", e)
            }
        }
    }
}
