package com.mhss.app.myeyes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import com.mhss.app.myeyes.ui.theme.MyEyesTheme
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mhss.app.myeyes.model.DetectedObject
import com.mhss.app.myeyes.presentation.BottomNavBar
import com.mhss.app.myeyes.presentation.CameraView
import com.mhss.app.myeyes.presentation.OverlayCanvas

const val OBJECT_DETECTOR = 0
const val TEXT_DETECTOR = 1
const val CURRENCY_DETECTOR = 2

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        setContent {
            MyEyesTheme {
                val cameraPermissionState = rememberPermissionState(
                    android.Manifest.permission.CAMERA
                )
                var resultsList by remember { mutableStateOf<List<DetectedObject>>(emptyList()) }
                var imgWidth by remember { mutableIntStateOf(0) }
                var imgHeight by remember { mutableIntStateOf(0) }
                var selectedModel by remember { mutableIntStateOf(OBJECT_DETECTOR) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0.dp)
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues = paddingValues)
                    ) {
                        if (cameraPermissionState.hasPermission) {

                            Scaffold(
                                bottomBar = {
                                    BottomNavBar(selectedOption = selectedModel) {
                                        selectedModel = it
                                    }
                                }
                            ) { paddingValues ->
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                ) {

                                    CameraView(
                                        modifier = Modifier.fillMaxSize(),
                                        aiModel = selectedModel,
                                        onResults = { results, imageHeight, imageWidth ->
                                            imgHeight = imageHeight
                                            imgWidth = imageWidth
                                            resultsList = results
                                        }
                                    )
                                    if (selectedModel == OBJECT_DETECTOR) {
                                        OverlayCanvas(
                                            modifier = Modifier.fillMaxSize(),
                                            results = resultsList,
                                            imgWidth = imgWidth,
                                            imgHeight = imgHeight
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("The camera is important for this feature. Please grant the permission.")
                                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                    Text("Request permission")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}