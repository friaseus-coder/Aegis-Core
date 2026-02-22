package com.antigravity.aegis.domain.services

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.data.local.entity.QuoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PdfGeneratorService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun generateQuotePdf(
        quote: QuoteEntity, 
        lines: List<BudgetLineEntity>, 
        client: Client?

    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Header
        paint.textSize = 24f
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("PRESUPUESTO", 50f, 60f, paint)
        
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("#${quote.id}", 50f, 85f, paint)
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("Fecha: ${dateFormat.format(Date(quote.date))}", 400f, 60f, paint)

        // Client Info
        paint.textSize = 12f
        canvas.drawText("Cliente:", 50f, 130f, paint)
        if (client != null) {
            val clientName = if (!client.razonSocial.isNullOrBlank()) client.razonSocial else "${client.firstName} ${client.lastName}".trim()
            val clientAddress = listOfNotNull(client.address?.calle, client.address?.numero, client.address?.poblacion, client.address?.codigoPostal).joinToString(", ")
            
            paint.isFakeBoldText = true
            canvas.drawText(clientName, 50f, 150f, paint)
            paint.isFakeBoldText = false
            canvas.drawText("DNI/CIF: ${client.nifCif ?: "-"}", 50f, 165f, paint)
            canvas.drawText("Dirección: $clientAddress", 50f, 180f, paint)

        } else {
             canvas.drawText("Cliente Desconocido", 50f, 150f, paint)
        }

        // Table Header
        val startY = 230f
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        canvas.drawRect(50f, startY, 545f, startY + 25f, paint)
        
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Descripción", 60f, startY + 18f, paint)
        canvas.drawText("Cant.", 350f, startY + 18f, paint)
        canvas.drawText("Precio", 400f, startY + 18f, paint)
        canvas.drawText("Total", 480f, startY + 18f, paint)

        // Table Rows
        var currentY = startY + 45f
        paint.isFakeBoldText = false
        
        lines.forEach { line ->
            canvas.drawText(line.description, 60f, currentY, paint)
            canvas.drawText(line.quantity.toString(), 350f, currentY, paint)
            canvas.drawText(String.format("€%.2f", line.unitPrice), 400f, currentY, paint)
            
            val lineTotal = line.quantity * line.unitPrice
            canvas.drawText(String.format("€%.2f", lineTotal), 480f, currentY, paint)
            
            currentY += 20f
        }
        
        // Horizontal Line
        paint.strokeWidth = 1f
        canvas.drawLine(50f, currentY + 10f, 545f, currentY + 10f, paint)

        // Totals
        currentY += 40f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Total Neto:", 350f, currentY, paint)
        val net = quote.totalAmount / 1.21
        canvas.drawText(String.format("€%.2f", net), 460f, currentY, paint)
        
        currentY += 20f
        canvas.drawText("IVA (21%):", 350f, currentY, paint)
        canvas.drawText(String.format("€%.2f", quote.totalAmount - net), 460f, currentY, paint)
        
        currentY += 25f
        paint.textSize = 16f
        canvas.drawText("TOTAL:", 350f, currentY, paint)
        canvas.drawText(String.format("€%.2f", quote.totalAmount), 460f, currentY, paint)

        drawLegalFooter(canvas, pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat())

        pdfDocument.finishPage(page)

        // Save
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Presupuesto_${quote.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return file
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
