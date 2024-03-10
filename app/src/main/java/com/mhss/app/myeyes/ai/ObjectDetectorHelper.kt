package com.mhss.app.myeyes.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.mhss.app.myeyes.model.DetectedObject
import com.mhss.app.myeyes.model.toDetectedObject
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ObjectDetectorHelper(
    private val threshold: Float = 0.5f,
    private val numThreads: Int = 2,
    private val maxResults: Int = 3,
    private val currentDelegate: Int = DELEGATE_CPU,
    val context: Context,
    val onResults: (List<DetectedObject>, Int, Int) -> Unit
) {

    private lateinit var objectDetector: ObjectDetector

    init {
        setupObjectDetector()
    }

    // Initialize the object detector using current settings on the
    // thread that is using it. CPU and NNAPI delegates can be used with detectors
    // that are created on the main thread and used on a background thread, but
    // the GPU delegate needs to be used on the thread that initialized the detector
    private fun setupObjectDetector() {
        val optionsBuilder =
            ObjectDetector.ObjectDetectorOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)

        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    baseOptionsBuilder.useGpu()
                }
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        val modelName = "efficientdet_lite1.tflite"

        try {
            objectDetector =
                ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build())
        } catch (e: IllegalStateException) {
            Toast.makeText(
                context,
                "Object detector failed to initialize. See error logs for details",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("ObjectDetector", "TFLite failed to load model with error: " + e.message)
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (!::objectDetector.isInitialized) {
            setupObjectDetector()
        }

        val imageProcessor =
            ImageProcessor.Builder()
                .add(Rot90Op(-imageRotation / 90))
                .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector.detect(tensorImage)?.map { it.toDetectedObject() } ?: emptyList()
        onResults(results, tensorImage.height, tensorImage.width)
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
    }
}