package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScreenState
import com.example.ui.PandaViewModel
import com.example.ui.components.AnimatedBackgroundOrbs
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidPandaSphere
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: PandaViewModel) {
    var loadingPercent by remember { mutableStateOf(12) }

    // Increment loading values for dynamic feel
    LaunchedEffect(Unit) {
        while (loadingPercent < 100) {
            delay(120)
            loadingPercent += (3..12).random()
            if (loadingPercent > 75 && loadingPercent < 90) {
                // Pause slightly around 75% to match the reference image's visual state!
                delay(300)
            }
        }
        loadingPercent = 100
        delay(400)
        // Transition to Onboarding
        viewModel.navigateTo(ScreenState.Onboarding)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen_root")
    ) {
        // Space Orbs backdrop
        AnimatedBackgroundOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Main Sphere & Branding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Large breathing Panda Core
                LiquidPandaSphere(
                    modifier = Modifier.size(240.dp),
                    textIndicator = "PANDA CORE v3.1"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Brand text
                Text(
                    text = "PANDA",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Personal AI Agent",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You relax, I handle the rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom loading progress bar wrapped in transparent rounded container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Initializing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Outer glass bar container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(0.5.dp, GlassBorder, RoundedCornerShape(4.dp))
                ) {
                    // Inner cyan gradient track matching the loader percentage
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(loadingPercent / 100f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(CyanGlow.copy(alpha = 0.4f), CyanGlow)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$loadingPercent%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanGlow,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
