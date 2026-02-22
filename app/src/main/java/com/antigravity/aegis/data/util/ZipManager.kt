package com.antigravity.aegis.data.util

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZipManager @Inject constructor() {

    fun zipFiles(files: List<File>, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
            for (file in files) {
                if (file.isDirectory) {
                    zipDirectory(file, file.name, out)
                } else if (file.exists()) {
                    zipFile(file, "", out)
                }
            }
        }
    }

    private fun zipDirectory(folder: File, parentName: String, out: ZipOutputStream) {
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                zipDirectory(file, "$parentName/${file.name}", out)
            } else {
                zipFile(file, parentName, out)
            }
        }
    }

    private fun zipFile(file: File, parentName: String, out: ZipOutputStream) {
        FileInputStream(file).use { fi ->
            val entryName = if (parentName.isEmpty()) file.name else "$parentName/${file.name}"
            val entry = ZipEntry(entryName)
            out.putNextEntry(entry)
            fi.copyTo(out)
        }
    }
}
