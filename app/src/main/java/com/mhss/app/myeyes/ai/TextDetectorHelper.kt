package com.mhss.app.myeyes.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mhss.app.myeyes.model.DetectedObject
import com.mhss.app.myeyes.model.toDetectedObject

class TextDetectorHelper(
    val onResults: (List<DetectedObject>, Int, Int) -> Unit
) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun detect(bitmap: Bitmap, rotation: Int) {
        val image = InputImage.fromBitmap(bitmap, rotation)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedObjects = visionText.textBlocks.map {
                    it.toDetectedObject()
                }
                onResults(detectedObjects, image.height, image.width)
            }
    }

}