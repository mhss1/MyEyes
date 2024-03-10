package com.mhss.app.myeyes.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mhss.app.myeyes.model.DetectedObject
import kotlin.math.max

@Composable
fun OverlayCanvas(
    modifier: Modifier = Modifier,
    results: List<DetectedObject>,
    imgWidth: Int,
    imgHeight: Int
) {
    Canvas(modifier) {
        val scaleFactor = max(size.width * 1f / imgWidth, size.height * 1f / imgHeight)
        results.forEach { obj ->
            obj.boundingBox?.apply {
                val left = left * scaleFactor
                val top = top * scaleFactor
                val right = right * scaleFactor
                val bottom = bottom * scaleFactor
                drawRoundRect(
                    color = Color.Cyan,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(4.dp.toPx(), join = StrokeJoin.Round)
                )
            }
        }
    }
}