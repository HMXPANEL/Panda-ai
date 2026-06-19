package com.example

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MediaProjectionHelper
import com.example.ui.PandaViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val projection = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
                .getMediaProjection(result.resultCode, result.data!!)
            MediaProjectionHelper.setProjection(projection)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MediaProjectionHelper.initScreenDimensions(application)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: PandaViewModel = viewModel()
                val activeScreen by viewModel.screen.collectAsState()

                // Observe screenshot permission requests
                val requestCapture by viewModel.requestScreenCapture.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Crossfade(
                        targetState = activeScreen,
                        animationSpec = tween(500),
                        label = "screen_routing"
                    ) { state ->
                        when (state) {
                            ScreenState.Splash -> SplashScreen(viewModel = viewModel)
                            ScreenState.Onboarding -> OnboardingScreen(viewModel = viewModel)
                            ScreenState.Permissions -> PermissionsScreen(viewModel = viewModel)
                            ScreenState.MainApp -> MainAppContainer(viewModel = viewModel)
                        }
                    }
                }

                // Launch screen capture permission request when ViewModel signals
                if (requestCapture) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        screenCaptureLauncher.launch(mpm.createScreenCaptureIntent())
                        viewModel.onScreenCaptureRequestHandled()
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
