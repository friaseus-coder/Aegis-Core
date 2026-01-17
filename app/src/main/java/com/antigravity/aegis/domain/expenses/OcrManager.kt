package com.antigravity.aegis.domain.expenses

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

class OcrManager @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun analyzeTicket(context: Context, imageUri: Uri): ExtractedData {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val visionText = recognizer.process(image).await()
            val text = visionText.text
            
            ExtractedData(
                rawText = text,
                totalAmount = extractTotal(text),
                date = extractDate(text)
            )
        } catch (e: IOException) {
            e.printStackTrace()
            ExtractedData("", null, null)
        }
    }

    private fun extractTotal(text: String): Double? {
        // Regex to find "TOTAL" followed by a number. 
        // Handles "TOTAL 12.50", "TOTAL: 12,50", "TOTAL $12.50"
        val pattern = Pattern.compile("(?:TOTAL|IMPORTE|SUMA).*?(\\d+[.,]\\d{2})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        
        if (matcher.find()) {
            val match = matcher.group(1)
            // Normalize: replace comma with dot if needed
            val normalized = match?.replace(",", ".")
            return normalized?.toDoubleOrNull()
        }
        
        // Fallback: search for the largest number that looks like a price at the end? 
        // For now, keep it valid only if explicit "Total" found.
        return null
    }

    private fun extractDate(text: String): Long? {
        // Regex for dd/MM/yyyy or dd-MM-yyyy
        val pattern = Pattern.compile("(\\d{2}[/.-]\\d{2}[/.-]\\d{4})")
        val matcher = pattern.matcher(text)
        
        if (matcher.find()) {
            val dateStr = matcher.group(1)
            return try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                // Handle different separators if necessary, basic normalization:
                val normalized = dateStr?.replace("-", "/")?.replace(".", "/")
                sdf.parse(normalized)?.time
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    data class ExtractedData(
        val rawText: String,
        val totalAmount: Double?,
        val date: Long?
    )
}
