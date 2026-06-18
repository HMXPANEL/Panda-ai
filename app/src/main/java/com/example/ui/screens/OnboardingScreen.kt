package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.ui.components.GradientButton
import com.example.ui.components.LiquidGlassButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.ui.components.LiquidPandaSphere
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun OnboardingScreen(viewModel: PandaViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen_root")
    ) {
        // Space blobs back-depth
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
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Meet Panda\nYour AI Agent",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Panda can understand, think,\nand act on your behalf.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Central holographic avatar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LiquidPandaSphere(
                    modifier = Modifier.size(240.dp),
                    textIndicator = "PANDA CORE"
                )
            }

            // Bottom action tray with carousel dots and Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Carousel dots on a frosted glass tray
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..3) {
                            val isActive = i == 1 // Screen 2 corresponds to onboarding dot 1
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) CyanGlow else Color.White.copy(alpha = 0.3f))
                                    .border(
                                        width = if (isActive) 1.dp else 0.dp,
                                        color = if (isActive) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Next Button
                LiquidGlassButton(
                    text = "Next",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        viewModel.navigateTo(ScreenState.Permissions)
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    }
}
