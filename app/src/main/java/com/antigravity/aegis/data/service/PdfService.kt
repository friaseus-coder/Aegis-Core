package com.antigravity.aegis.data.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class PdfService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun generateQuotePdf(quote: QuoteEntity, lines: List<BudgetLineEntity>): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points (approx)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Styles
        val titlePaint = Paint().apply { 
            color = Color.BLACK 
            textSize = 24f 
            isFakeBoldText = true 
        }
        val textPaint = Paint().apply { 
            color = Color.BLACK 
            textSize = 12f 
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isFakeBoldText = true
        }

        // Header
        canvas.drawText("PRESUPUESTO", 50f, 50f, titlePaint)
        canvas.drawText("Ref: #${quote.id}", 50f, 80f, textPaint)
        canvas.drawText("Fecha: ${formatDate(quote.date)}", 50f, 100f, textPaint)
        canvas.drawText("Proyecto: ${quote.title}", 50f, 120f, textPaint)

        // Wrapper for Client Info (Ideally fetch client details passed as arg, using ID for now)
        canvas.drawText("Cliente ID: ${quote.clientId}", 300f, 80f, textPaint)

        // Line Items Header
        var yPosition = 160f
        canvas.drawText("Descripción", 50f, yPosition, headerPaint)
        canvas.drawText("Cant.", 350f, yPosition, headerPaint)
        canvas.drawText("Precio", 420f, yPosition, headerPaint)
        canvas.drawText("Total", 500f, yPosition, headerPaint)
        
        yPosition += 20f
        paint.color = Color.LTGRAY
        canvas.drawLine(50f, yPosition, 550f, yPosition, paint)
        yPosition += 20f

        // Lines
        var subtotal = 0.0
        lines.forEach { line ->
            val totalLine = line.quantity * line.unitPrice
            subtotal += totalLine

            canvas.drawText(line.description.take(40), 50f, yPosition, textPaint)
            canvas.drawText(String.format("%.2f", line.quantity), 350f, yPosition, textPaint)
            canvas.drawText(String.format("%.2f€", line.unitPrice), 420f, yPosition, textPaint)
            canvas.drawText(String.format("%.2f€", totalLine), 500f, yPosition, textPaint)
            
            yPosition += 20f
        }

        yPosition += 20f
        canvas.drawLine(50f, yPosition, 550f, yPosition, paint)
        yPosition += 30f

        // Totals
        val taxes = subtotal * 0.21 // Assuming 21% fixed for now or calculated from lines
        val total = subtotal + taxes

        canvas.drawText("Subtotal: ${String.format("%.2f€", subtotal)}", 400f, yPosition, textPaint)
        yPosition += 20f
        canvas.drawText("IVA (21%): ${String.format("%.2f€", taxes)}", 400f, yPosition, textPaint)
        yPosition += 20f
        
        val totalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }
        canvas.drawText("TOTAL: ${String.format("%.2f€", total)}", 400f, yPosition, totalPaint)

        drawLegalFooter(canvas, pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat())

        document.finishPage(page)

        // Save
        val fileName = "Presupuesto_${quote.id}_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        return try {
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    fun generateExpenseReport(expenses: List<ExpenseEntity>): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        
        // Page 1: List
        val page1 = document.startPage(pageInfo)
        val canvas = page1.canvas
        val textPaint = Paint().apply { textSize = 10f; color = Color.BLACK }
        val titlePaint = Paint().apply { textSize = 20f; color = Color.BLACK; isFakeBoldText = true }

        canvas.drawText("REPORTE DE GASTOS", 50f, 50f, titlePaint)
        
        var y = 100f
        canvas.drawText("Fecha", 50f, y, textPaint)
        canvas.drawText("Concepto", 150f, y, textPaint)
        canvas.drawText("Categoría", 350f, y, textPaint)
        canvas.drawText("Importe", 500f, y, textPaint)
        y += 20f
        
        var totalAmount = 0.0
        
        expenses.forEach { expense ->
            if (y > 800f) {
                // Should start new page, simpler implementation truncates for now
                return@forEach
            }
            canvas.drawText(formatDate(expense.date), 50f, y, textPaint)
            canvas.drawText((expense.merchantName ?: "Sin nombre").take(30), 150f, y, textPaint)
            canvas.drawText(expense.category.take(20), 350f, y, textPaint)
            canvas.drawText("${expense.totalAmount}€", 500f, y, textPaint)
            totalAmount += expense.totalAmount
            y += 15f
        }
        
        y += 20f
        canvas.drawText("TOTAL: ${String.format("%.2f€", totalAmount)}", 400f, y, titlePaint)
        
        drawLegalFooter(canvas, pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat())

        document.finishPage(page1)
        
        // Could ensure "Dossier" pages here for images
        // ...

        val fileName = "Gastos_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        return try {
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    private fun drawLegalFooter(canvas: Canvas, pageWidth: Float, pageHeight: Float) {
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 7f
            isAntiAlias = true
        }
        val text = "Documento firmado electrónicamente de mutuo acuerdo mediante la plataforma Aegis. Esta firma tiene carácter de cortesía y no constituye un certificado digital cualificado."
        val margin = 50f
        val x = margin
        val y = pageHeight - 30f
        canvas.drawText(text, x, y, paint)
    }
}
