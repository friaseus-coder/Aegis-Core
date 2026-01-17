package com.antigravity.aegis.presentation.reports

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    onSignatureChanged: (androidx.compose.ui.graphics.Path) -> Unit
) {
    // Basic implementation using Path
    // For saving to Bitmap, we might need a more View-based approach or capturing the Canvas
    // To simplify for this specific task of "PDF generation requiring Bitmap", 
    // wrapping a custom View is often easier to extract the Bitmap than pure Compose Canvas 
    // without using specific capture libraries.
    
    // However, let's try a pure Compose path approach first and we can generate Bitmap from Path logic later 
    // OR we can use AndroidView with a custom helper view which is robust.
    
    // Let's use a Custom View approach for easier Bitmap extraction.
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, Color.Gray)
            .background(Color.White),
        factory = { context ->
            SignatureView(context)
        },
        update = { view ->
            // Update logic if needed
        }
    )
}

class SignatureView(context: android.content.Context) : android.view.View(context) {
    private val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        isDither = true
        color = android.graphics.Color.BLACK
        style = android.graphics.Paint.Style.STROKE
        strokeJoin = android.graphics.Paint.Join.ROUND
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeWidth = 10f
    }

    private val path = android.graphics.Path()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val dirtyRect = android.graphics.RectF()

    fun clear() {
        path.reset()
        invalidate()
    }

    fun getSignatureBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawPath(path, paint)
        return bitmap
    }
    
    fun isEmpty(): Boolean = path.isEmpty

    override fun onDraw(canvas: android.graphics.Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                resetDirtyRect(eventX, eventY)
                val historySize = event.historySize
                for (i in 0 until historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                }
                path.lineTo(eventX, eventY)
                invalidate(
                    (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
                )
                lastTouchX = eventX
                lastTouchY = eventY
            }
        }
        return true
    }

    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < dirtyRect.left) dirtyRect.left = historicalX
        else if (historicalX > dirtyRect.right) dirtyRect.right = historicalX
        if (historicalY < dirtyRect.top) dirtyRect.top = historicalY
        else if (historicalY > dirtyRect.bottom) dirtyRect.bottom = historicalY
    }

    private fun resetDirtyRect(eventX: Float, eventY: Float) {
        dirtyRect.left = minOf(lastTouchX, eventX)
        dirtyRect.right = maxOf(lastTouchX, eventX)
        dirtyRect.top = minOf(lastTouchY, eventY)
        dirtyRect.bottom = maxOf(lastTouchY, eventY)
    }

    companion object {
        private const val HALF_STROKE_WIDTH = 5f
    }
}
