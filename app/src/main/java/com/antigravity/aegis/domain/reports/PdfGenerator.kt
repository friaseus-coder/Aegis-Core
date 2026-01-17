package com.antigravity.aegis.domain.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.antigravity.aegis.data.model.ClientEntity
import com.antigravity.aegis.data.model.ProjectEntity
import com.antigravity.aegis.data.model.WorkReportEntity
import com.antigravity.aegis.data.model.QuoteEntity
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfGenerator @Inject constructor() {

    fun generateReportPdf(
        context: Context,
        report: WorkReportEntity,
        project: ProjectEntity,
        client: ClientEntity,
        signatureBitmap: android.graphics.Bitmap?
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("WORK REPORT", 50f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Report ID: ${report.id}", 50f, 80f, paint)
        canvas.drawText("Date: ${java.util.Date(report.date)}", 50f, 100f, paint)
        
        canvas.drawText("Client: ${client.name}", 50f, 140f, paint)
        canvas.drawText("Project: ${project.name}", 50f, 160f, paint)

        canvas.drawText("Description:", 50f, 200f, paint)
        
        // Simple multiline text simulation
        val descriptionLines = report.description.split("\n")
        var yPos = 220f
        for (line in descriptionLines) {
            canvas.drawText(line, 50f, yPos, paint)
            yPos += 20f
        }

        yPos += 40f
        paint.isFakeBoldText = true
        canvas.drawText("Signature:", 50f, yPos, paint)
        
        if (signatureBitmap != null) {
            // Scale bitmap to fit
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(signatureBitmap, 200, 100, true)
            canvas.drawBitmap(scaledBitmap, 50f, yPos + 10f, null)
        } else {
             paint.isFakeBoldText = false
             canvas.drawText("[No Signature]", 50f, yPos + 20f, paint)
        }

        pdfDocument.finishPage(page)

        val directory = File(context.filesDir, "reports")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, "Report_${report.id}.pdf")
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return file
    }
    fun generateQuotePdf(
        context: Context,
        quote: QuoteEntity,
        client: ClientEntity
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("BUDGET / QUOTE", 50f, 50f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Quote ID: ${quote.id}", 50f, 80f, paint)
        canvas.drawText("Date: ${java.util.Date(quote.date)}", 50f, 100f, paint)
        canvas.drawText("Status: ${quote.status}", 50f, 120f, paint)
        
        canvas.drawText("Client: ${client.name}", 50f, 160f, paint)
        if (!client.email.isNullOrEmpty()) canvas.drawText("Email: ${client.email}", 50f, 180f, paint)

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Title: ${quote.title}", 50f, 230f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Description/Items:", 50f, 260f, paint)
        
        val descriptionLines = quote.description.split("\n")
        var yPos = 280f
        for (line in descriptionLines) {
            canvas.drawText(line, 50f, yPos, paint)
            yPos += 20f
        }

        yPos += 40f
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Total Amount: $${quote.totalAmount}", 50f, yPos, paint)

        pdfDocument.finishPage(page)

        val directory = File(context.filesDir, "quotes")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, "Quote_${quote.id}.pdf")
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return file
    }
}
