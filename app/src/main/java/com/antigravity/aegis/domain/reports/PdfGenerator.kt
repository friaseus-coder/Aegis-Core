package com.antigravity.aegis.domain.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import java.io.File
import java.io.FileOutputStream
import com.antigravity.aegis.data.local.entity.UserConfig
import android.graphics.BitmapFactory
import javax.inject.Inject

class PdfGenerator @Inject constructor() {
    
    fun generateQuotePdf(
        context: Context,
        quote: QuoteEntity,
        client: ClientEntity,
        config: UserConfig?
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var yPos = 50f
        
        // --- COMPANY HEADER ---
        if (config != null) {
            // Logo
            if (!config.companyLogoUri.isNullOrBlank()) {
                val logoFile = File(config.companyLogoUri)
                if (logoFile.exists()) {
                    val logoBitmap = BitmapFactory.decodeFile(config.companyLogoUri)
                    if (logoBitmap != null) {
                          try {
                             val aspectRatio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
                             val width = if (aspectRatio > 1) 80 else (80 * aspectRatio).toInt()
                             val height = if (aspectRatio > 1) (80 / aspectRatio).toInt() else 80
                             
                             val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logoBitmap, width, height, true)
                             canvas.drawBitmap(scaledLogo, 50f, yPos, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            // Company Details
            paint.color = Color.BLACK
            paint.textSize = 10f // Small text for details
            paint.isFakeBoldText = true
            
            var textY = if (!config.companyLogoUri.isNullOrBlank()) yPos + 90f else yPos
            
            if (config.companyName.isNotBlank()) {
                paint.isFakeBoldText = true
                canvas.drawText(config.companyName, 50f, textY, paint)
                textY += 12f
            }
            
            paint.isFakeBoldText = false
             if (config.companyDniCif.isNotBlank()) {
                 canvas.drawText("CIF/NIF: ${config.companyDniCif}", 50f, textY, paint)
                 textY += 12f
            }
            if (config.companyAddress.isNotBlank()) {
                 canvas.drawText(config.companyAddress, 50f, textY, paint)
                 textY += 12f
            }
             if (config.companyPostalCode.isNotBlank() || config.companyCity.isNotBlank()) {
                 canvas.drawText("${config.companyPostalCode} ${config.companyCity}", 50f, textY, paint)
                 textY += 12f
            }
             if (config.companyProvince.isNotBlank()) {
                 canvas.drawText(config.companyProvince, 50f, textY, paint)
                 textY += 12f
            }
            
            // Adjust main content start position
            yPos = textY + 20f
        } else {
             yPos = 50f
        }

        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("BUDGET / QUOTE", 50f, yPos + 24f, paint)
        yPos += 60f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Quote ID: ${quote.id}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Date: ${java.util.Date(quote.date)}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Status: ${quote.status}", 50f, yPos, paint)
        yPos += 40f
        
        val clientName = if (client.tipoCliente == "Particular") "${client.firstName} ${client.lastName}" else client.firstName
        canvas.drawText("Client: $clientName", 50f, yPos, paint)
        yPos += 20f
        if (!client.email.isNullOrEmpty()) canvas.drawText("Email: ${client.email}", 50f, yPos, paint) else yPos -= 20f // Adjust if no email?
        yPos += 40f

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Title: ${quote.title}", 50f, yPos, paint)
        yPos += 30f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Description/Items:", 50f, yPos, paint)
        yPos += 20f
        
        val descriptionLines = quote.description.split("\n")
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
