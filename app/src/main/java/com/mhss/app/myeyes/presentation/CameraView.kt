package com.mhss.app.myeyes.presentation

import android.graphics.Bitmap
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mhss.app.myeyes.CURRENCY_DETECTOR
import com.mhss.app.myeyes.OBJECT_DETECTOR
import com.mhss.app.myeyes.ai.ObjectDetectorHelper
import com.mhss.app.myeyes.model.DetectedObject
import com.mhss.app.myeyes.util.toCurrencySummary
import java.util.Locale

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    aiModel: Int,
    onResults: (List<DetectedObject>, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var cameraSelector: CameraSelector? by remember { mutableStateOf(null) }
    var imageAnalyzer: ImageAnalysis?
    var preview by remember { mutableStateOf<Preview?>(null) }
    var bitmapBuffer: Bitmap? = remember { null }
    var currentObjectsSet by remember { mutableStateOf<Set<String>>(setOf()) }
    var currentCurrencyMap by remember { mutableStateOf<Map<String, Int>>(mapOf()) }
    var objectDetector: ObjectDetectorHelper? = null
    var tts: TextToSpeech? = null

    LaunchedEffect(aiModel) {
        objectDetector?.setModel(aiModel)
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.BLACK)
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_START
            }
        },
        update = { view ->

            tts = TextToSpeech(
                context
            ) { status ->
                if (status != TextToSpeech.ERROR) {
                    tts?.language = Locale.US
                }
            }

            var processed = 0
            objectDetector = ObjectDetectorHelper(context) { results, width, height ->
                    onResults(results, width, height)
                    if (aiModel == OBJECT_DETECTOR) {
                        val resultsSet = results.map { it.label }.toSet()
                        val diff = resultsSet - currentObjectsSet
                        if (diff.isNotEmpty()) tts?.stop()
                        diff.forEach { txt ->
                            tts?.speak(
                                txt,
                                TextToSpeech.QUEUE_ADD,
                                null,
                                null
                            )
                        }
                        currentObjectsSet += diff
                        if (processed++ % 120 == 0) {
                            currentObjectsSet = setOf()
                        }
                        currentCurrencyMap = mapOf()
                    } else {
                        val currenciesMap = results.groupBy { it.label }.mapValues { it.value.size }
                        if (currenciesMap.isNotEmpty() && currenciesMap != currentCurrencyMap && currenciesMap.size > currentCurrencyMap.size) {
                            val summary = currenciesMap.toCurrencySummary()
                            tts?.speak(
                                summary,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                null
                            )
                            currentCurrencyMap = currenciesMap
                        }
                        if (processed++ % 120 == 0) {
                            currentCurrencyMap = mapOf()
                        }
                        currentObjectsSet = setOf()
                    }
                }

            var imageRotationDegrees = 0

            view.post {
                cameraProviderFuture.addListener(
                    {
                        val cProvider =
                            cameraProvider
                                ?: throw IllegalStateException(
                                    "Camera initialization failed."
                                )

                        cameraSelector =
                            CameraSelector.Builder()
                                .requireLensFacing(
                                    CameraSelector.LENS_FACING_BACK
                                ).build()

                        preview =
                            Preview.Builder()
                                .setTargetAspectRatio(
                                    AspectRatio.RATIO_4_3
                                )
                                .setTargetRotation(
                                    view.display.rotation
                                )
                                .build()


                        imageAnalyzer =
                            ImageAnalysis.Builder()
                                .setTargetAspectRatio(
                                    AspectRatio.RATIO_4_3
                                )
                                .setTargetRotation(
                                    view.display.rotation
                                )
                                .setBackpressureStrategy(
                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .setOutputImageFormat(
                                    ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                                )
                                .build()
                                .also { analyzer ->
                                    analyzer.setAnalyzer(
                                        executor
                                    ) { image ->
                                        if (bitmapBuffer == null) {
                                            imageRotationDegrees =
                                                image.imageInfo.rotationDegrees
                                            bitmapBuffer =
                                                Bitmap.createBitmap(
                                                    image.width,
                                                    image.height,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                        }

                                        // Copy out RGB bits to our shared buffer
                                        image.use {
                                            bitmapBuffer?.copyPixelsFromBuffer(
                                                image.planes[0].buffer
                                            )
                                        }

                                        if (aiModel == OBJECT_DETECTOR || aiModel == CURRENCY_DETECTOR) {
                                            objectDetector?.detect(
                                                bitmapBuffer!!,
                                                imageRotationDegrees
                                            )
                                        } else {

                                        }
                                    }
                                }

                        cProvider.unbindAll()

                        try {
                            cProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector!!,
                                preview,
                                imageAnalyzer
                            )

                            preview?.setSurfaceProvider(
                                view.surfaceProvider
                            )


                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    },
                    executor
                )
            }
        }
    )
}