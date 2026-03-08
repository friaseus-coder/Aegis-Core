package com.antigravity.aegis.domain.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.UserConfig
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Genera PDFs de presupuestos con layout profesional:
 * - Columna izquierda: datos del proveedor (empresa)
 * - Columna derecha superior: datos del cliente
 * - Título central: PRESUPUESTO Nº + fecha
 * - Tabla de subproyectos/conceptos con precios
 * - Totales al pie (base imponible, IVA, TOTAL)
 * - Pie de página legal
 */
class PdfGenerator @Inject constructor() {

    companion object {
        private const val PAGE_WIDTH = 595f   // A4 horizontal en puntos
        private const val PAGE_HEIGHT = 842f  // A4 vertical en puntos
        private const val MARGIN = 40f
        private const val COL_RIGHT_START = PAGE_WIDTH / 2f + 10f
        private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    /**
     * Genera el PDF del presupuesto con el layout completo.
     *
     * @param context       Contexto Android para acceder a filesDir
     * @param quote         Entidad QuoteEntity a generar
     * @param client        Cliente asociado al presupuesto
     * @param config        Configuración de empresa (logo, CIF, dirección...)
     * @param subProjects   Subproyectos del proyecto asociado (como conceptos)
     * @param budgetLines   Líneas de presupuesto (alternativo a subProjects si no hay proyecto asociado)
     * @return File con el PDF generado
     */
    fun generateQuotePdf(
        context: Context,
        quote: QuoteEntity,
        client: Client,
        config: UserConfig? = null,
        budgetLines: List<BudgetLineEntity> = emptyList()
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var yPos = MARGIN

        // ─────────────────────────────────────────────────────────────────
        // 1. CABECERA: PROVEEDOR (izquierda) | CLIENTE (derecha)
        // ─────────────────────────────────────────────────────────────────
        val paint = Paint().apply { isAntiAlias = true }

        // --- LOGO (izquierda superior) ---
        var leftY = yPos
        if (config != null && !config.companyLogoUri.isNullOrBlank()) {
            val logoFile = File(config.companyLogoUri)
            if (logoFile.exists()) {
                val logoBitmap = BitmapFactory.decodeFile(config.companyLogoUri)
                if (logoBitmap != null) {
                    try {
                        val maxH = 60
                        val aspectRatio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
                        val logoW = (maxH * aspectRatio).toInt()
                        val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logoBitmap, logoW, maxH, true)
                        canvas.drawBitmap(scaledLogo, MARGIN, leftY, null)
                        leftY += maxH + 6f
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }

        // --- DATOS PROVEEDOR (izquierda) ---
        paint.textSize = 9f
        paint.color = Color.BLACK
        if (config != null) {
            if (config.companyName.isNotBlank()) {
                paint.isFakeBoldText = true
                paint.textSize = 11f
                canvas.drawText(config.companyName, MARGIN, leftY + 11f, paint)
                leftY += 15f
                paint.isFakeBoldText = false
                paint.textSize = 9f
            }
            if (config.companyDniCif.isNotBlank()) {
                canvas.drawText("CIF/NIF: ${config.companyDniCif}", MARGIN, leftY + 9f, paint)
                leftY += 12f
            }
            if (config.companyAddress.isNotBlank()) {
                canvas.drawText(config.companyAddress, MARGIN, leftY + 9f, paint)
                leftY += 12f
            }
            val cityLine = buildString {
                if (config.companyPostalCode.isNotBlank()) append(config.companyPostalCode).append(" ")
                if (config.companyCity.isNotBlank()) append(config.companyCity)
            }
            if (cityLine.isNotBlank()) {
                canvas.drawText(cityLine, MARGIN, leftY + 9f, paint)
                leftY += 12f
            }
            if (config.companyProvince.isNotBlank()) {
                canvas.drawText(config.companyProvince, MARGIN, leftY + 9f, paint)
                leftY += 12f
            }
        }

        // --- DATOS CLIENTE (derecha superior) ---
        var rightY = yPos
        paint.textSize = 9f
        paint.color = Color.parseColor("#555555")
        canvas.drawText("CLIENTE", COL_RIGHT_START, rightY + 9f, paint)
        rightY += 14f

        paint.color = Color.BLACK
        val clientName = when {
            !client.razonSocial.isNullOrBlank() -> client.razonSocial
            client.tipoCliente == ClientType.EMPRESA -> client.firstName
            else -> "${client.firstName} ${client.lastName}".trim()
        }
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText(clientName, COL_RIGHT_START, rightY + 11f, paint)
        rightY += 15f
        paint.isFakeBoldText = false
        paint.textSize = 9f

        if (!client.nifCif.isNullOrBlank()) {
            canvas.drawText("CIF/NIF: ${client.nifCif}", COL_RIGHT_START, rightY + 9f, paint)
            rightY += 12f
        }
        val clientAddress = listOfNotNull(
            client.address?.calle,
            client.address?.numero,
            client.address?.codigoPostal,
            client.address?.poblacion
        ).joinToString(", ")
        if (clientAddress.isNotBlank()) {
            canvas.drawText(clientAddress, COL_RIGHT_START, rightY + 9f, paint)
            rightY += 12f
        }
        if (!client.email.isNullOrBlank()) {
            canvas.drawText(client.email, COL_RIGHT_START, rightY + 9f, paint)
            rightY += 12f
        }
        if (!client.phone.isNullOrBlank()) {
            canvas.drawText("Tel: ${client.phone}", COL_RIGHT_START, rightY + 9f, paint)
            rightY += 12f
        }

        // Avanzar al máximo de las dos columnas
        yPos = maxOf(leftY, rightY) + 20f

        // ─────────────────────────────────────────────────────────────────
        // 2. LÍNEA SEPARADORA
        // ─────────────────────────────────────────────────────────────────
        paint.color = Color.parseColor("#CCCCCC")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, paint)
        yPos += 16f

        // ─────────────────────────────────────────────────────────────────
        // 3. TÍTULO: PRESUPUESTO Nº + FECHA
        // ─────────────────────────────────────────────────────────────────
        paint.color = Color.parseColor("#1A3A5C")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("PRESUPUESTO Nº ${quote.id}", MARGIN, yPos + 20f, paint)
        yPos += 30f

        paint.isFakeBoldText = false
        paint.textSize = 10f
        paint.color = Color.parseColor("#444444")
        canvas.drawText("Fecha: ${DATE_FORMAT.format(Date(quote.date))}", MARGIN, yPos + 10f, paint)
        canvas.drawText("Estado: ${quote.status}", COL_RIGHT_START, yPos + 10f, paint)
        yPos += 24f

        if (quote.title.isNotBlank()) {
            paint.textSize = 12f
            paint.color = Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("Proyecto: ${quote.title}", MARGIN, yPos + 12f, paint)
            paint.isFakeBoldText = false
            yPos += 20f
        }

        if (!quote.description.isNullOrBlank() && quote.description != "Presupuesto generado desde proyecto: ${quote.title}") {
            paint.textSize = 9f
            paint.color = Color.parseColor("#666666")
            canvas.drawText(quote.description, MARGIN, yPos + 9f, paint)
            yPos += 16f
        }
        yPos += 8f

        // ─────────────────────────────────────────────────────────────────
        // 4. TABLA DE CONCEPTOS
        // ─────────────────────────────────────────────────────────────────
        // Cabecera de tabla
        paint.color = Color.parseColor("#1A3A5C")
        paint.style = Paint.Style.FILL
        canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        val colDesc = MARGIN + 6f
        val colQty = PAGE_WIDTH - MARGIN - 180f
        val colPrice = PAGE_WIDTH - MARGIN - 110f
        val colTotal = PAGE_WIDTH - MARGIN - 40f
        canvas.drawText("Descripción / Concepto", colDesc, yPos + 15f, paint)
        canvas.drawText("Cant.", colQty, yPos + 15f, paint)
        canvas.drawText("Precio", colPrice, yPos + 15f, paint)
        canvas.drawText("Total", colTotal, yPos + 15f, paint)
        yPos += 26f

        // Filas de la tabla
        paint.isFakeBoldText = false
        paint.textSize = 9f

        // Preferimos usar siempre budgetLines ya que son los conceptos reales de la Quote
        var grandTotal = 0.0
        var rowBg = false

        if (budgetLines.isNotEmpty()) {
            for (line in budgetLines) {
                if (rowBg) {
                    paint.color = Color.parseColor("#F5F5F5")
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 18f, paint)
                }
                paint.style = Paint.Style.FILL
                paint.color = Color.BLACK
                canvas.drawText(line.description.take(60), colDesc, yPos + 13f, paint)
                canvas.drawText("%.2f".format(line.quantity), colQty + 4f, yPos + 13f, paint)
                canvas.drawText("%.2f €".format(line.unitPrice), colPrice, yPos + 13f, paint)
                val lineTotal = line.quantity * line.unitPrice
                grandTotal += lineTotal
                canvas.drawText("%.2f €".format(lineTotal), colTotal, yPos + 13f, paint)
                yPos += 20f
                rowBg = !rowBg
                if (yPos > PAGE_HEIGHT - 120f) break
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // 5. TOTALES
        // ─────────────────────────────────────────────────────────────────
        yPos += 10f
        paint.color = Color.parseColor("#CCCCCC")
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, paint)
        paint.style = Paint.Style.FILL
        yPos += 16f

        val iva = grandTotal * 0.21
        val total = grandTotal + iva

        paint.textSize = 10f
        paint.color = Color.parseColor("#333333")

        // Base imponible
        paint.isFakeBoldText = false
        canvas.drawText("Base imponible:", PAGE_WIDTH - MARGIN - 180f, yPos, paint)
        canvas.drawText("%.2f €".format(grandTotal), PAGE_WIDTH - MARGIN - 40f, yPos, paint)
        yPos += 16f

        // IVA
        canvas.drawText("IVA (21%):", PAGE_WIDTH - MARGIN - 180f, yPos, paint)
        canvas.drawText("%.2f €".format(iva), PAGE_WIDTH - MARGIN - 40f, yPos, paint)
        yPos += 18f

        // TOTAL (destacado)
        paint.color = Color.parseColor("#1A3A5C")
        paint.style = Paint.Style.FILL
        canvas.drawRect(PAGE_WIDTH - MARGIN - 200f, yPos - 4f, PAGE_WIDTH - MARGIN, yPos + 18f, paint)
        paint.color = Color.WHITE
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL:", PAGE_WIDTH - MARGIN - 190f, yPos + 12f, paint)
        canvas.drawText("%.2f €".format(total), PAGE_WIDTH - MARGIN - 70f, yPos + 12f, paint)

        // ─────────────────────────────────────────────────────────────────
        // 6. PIE DE PÁGINA LEGAL
        // ─────────────────────────────────────────────────────────────────
        drawLegalFooter(canvas, PAGE_WIDTH, PAGE_HEIGHT)

        pdfDocument.finishPage(page)

        // Guardar en filesDir/quotes/
        val directory = File(context.filesDir, "quotes")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "Presupuesto_${quote.id}.pdf")

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
            color = Color.parseColor("#999999")
            textSize = 7f
            isAntiAlias = true
        }
        val line1 = "Documento generado por Aegis · Presupuesto sin carácter contractual hasta su aceptación firmada por ambas partes."
        val line2 = "Firma electrónica de cortesía: este documento no constituye un certificado digital cualificado según el Reglamento eIDAS."
        val y1 = pageHeight - 22f
        val y2 = pageHeight - 12f
        canvas.drawLine(MARGIN, y1 - 6f, pageWidth - MARGIN, y1 - 6f, paint.apply { strokeWidth = 0.5f; style = Paint.Style.STROKE })
        paint.style = Paint.Style.FILL
        canvas.drawText(line1, MARGIN, y1, paint)
        canvas.drawText(line2, MARGIN, y2, paint)
    }
}
