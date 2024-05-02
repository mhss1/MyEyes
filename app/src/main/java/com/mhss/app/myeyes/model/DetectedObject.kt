package com.mhss.app.myeyes.model

import android.graphics.RectF
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.text.Text.TextBlock
import org.tensorflow.lite.task.vision.detector.Detection

data class DetectedObject(
    val label: String,
    val boundingBox: RectF? = null
)

fun Detection.toDetectedObject(): DetectedObject {
    return DetectedObject(
        categories.first().label,
        boundingBox
    )
}

fun TextBlock.toDetectedObject(): DetectedObject {
    return DetectedObject(
        text,
        boundingBox?.toRectF()
    )
}