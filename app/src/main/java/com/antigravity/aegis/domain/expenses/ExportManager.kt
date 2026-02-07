package com.antigravity.aegis.domain.expenses

import android.content.Context
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ExportManager @Inject constructor() {

    fun exportToZip(context: Context, expenses: List<ExpenseEntity>, startDate: Long, endDate: Long): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        // 1. Create CSV
        val csvFile = File(exportDir, "expenses_report.csv")
        FileWriter(csvFile).use { writer ->
            writer.append("ID,Date,Merchant,Amount,Status\n")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            for (expense in expenses) {
                writer.append("${expense.id},${sdf.format(java.util.Date(expense.date))},\"${expense.merchantName}\",${expense.totalAmount},${expense.status}\n")
            }
        }

        // 2. Create ZIP
        val zipFile = File(exportDir, "QuarterExport_${System.currentTimeMillis()}.zip")
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                // Add CSV
                addFileToZip(zos, csvFile, "expenses.csv")

                // Add Images
                for (expense in expenses) {
                    if (!expense.imagePath.isNullOrEmpty()) {
                        val imgFile = File(expense.imagePath)
                        if (imgFile.exists()) {
                            addFileToZip(zos, imgFile, "images/expense_${expense.id}.jpg")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return zipFile
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, entryName: String) {
        val buffer = ByteArray(1024)
        FileInputStream(file).use { fis ->
            BufferedInputStream(fis).use { bis ->
                val entry = ZipEntry(entryName)
                zos.putNextEntry(entry)
                var length: Int
                while (bis.read(buffer).also { length = it } > 0) {
                    zos.write(buffer, 0, length)
                }
                zos.closeEntry()
            }
        }
    }
}
