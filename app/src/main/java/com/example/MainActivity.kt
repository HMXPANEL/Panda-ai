package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PandaViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: PandaViewModel = viewModel()
                val activeScreen by viewModel.screen.collectAsState()

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
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
