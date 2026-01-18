package com.antigravity.aegis.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.antigravity.aegis.ui.theme.Gold

@Composable
fun BovedaLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(48.dp)) {
        val strokeWidth = 3.dp.toPx()
        val color = Gold

        // Draw Shield/Safe Outline
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.9f, size.height * 0.25f)
            lineTo(size.width * 0.9f, size.height * 0.6f)
            cubicTo(
                size.width * 0.9f, size.height * 0.85f,
                size.width * 0.5f, size.height * 0.95f,
                size.width * 0.5f, size.height * 0.95f
            )
            cubicTo(
                size.width * 0.5f, size.height * 0.95f,
                size.width * 0.1f, size.height * 0.85f,
                size.width * 0.1f, size.height * 0.6f
            )
            lineTo(size.width * 0.1f, size.height * 0.25f)
            close()
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Draw Lock Mechanism (Circle)
        drawCircle(
            color = color,
            radius = size.width * 0.15f,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Draw Keyhole
        drawCircle(
            color = color,
            radius = size.width * 0.04f,
            center = center
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y + size.height * 0.1f),
            strokeWidth = strokeWidth
        )
    }
}
