package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.focus.onFocusChanged
import kotlin.math.cos
import kotlin.math.sin

// ============================================================
// AnimatedBackgroundOrbs — Animated cyan + purple + pink liquid glass orbs
// Drifts fluidly in slow motion using real blur effects on modern android
// ============================================================
@Composable
fun AnimatedBackgroundOrbs(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs_motion")
    
    // Complex, independent frequencies to create organic liquid flow (no static rotation)
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        // High quality glass mesh blur using render effect on Android 12+
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            renderEffect = android.graphics.RenderEffect
                                .createBlurEffect(160f, 160f, android.graphics.Shader.TileMode.MIRROR)
                                .asComposeRenderEffect()
                        } catch (e: Throwable) {
                            // Ignored
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Base background color inside blur layer
                drawRect(color = DeepSpace)

                // Orb 1: Cyan Glow - upper drift, floating organically
                val cyanOffsetX = size.width * (0.3f + 0.35f * cos(phase1.toDouble()).toFloat())
                val cyanOffsetY = size.height * (0.2f + 0.25f * sin(phase2.toDouble()).toFloat())
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.55f),
                    center = Offset(cyanOffsetX, cyanOffsetY),
                    radius = size.width * 0.55f
                )

                // Orb 2: Purple aura blob - middle-right bottom drift
                val purpleOffsetX = size.width * (0.7f - 0.25f * sin((phase2 + 1.2).toDouble()).toFloat())
                val purpleOffsetY = size.height * (0.6f + 0.2f * cos((phase1 + 0.8).toDouble()).toFloat())
                drawCircle(
                    color = PurpleAccent.copy(alpha = 0.45f),
                    center = Offset(purpleOffsetX, purpleOffsetY),
                    radius = size.width * 0.6f
                )

                // Orb 3: Radiant Magenta/Pink blob - center-left lower drift
                val pinkOffsetX = size.width * (0.4f + 0.2f * sin((phase1 + 2.5).toDouble()).toFloat())
                val pinkOffsetY = size.height * (0.75f - 0.2f * cos((phase3 - 1.5).toDouble()).toFloat())
                drawCircle(
                    color = NeonPink.copy(alpha = 0.45f),
                    center = Offset(pinkOffsetX, pinkOffsetY),
                    radius = size.width * 0.5f
                )

                // Orb 4: Tech-Blue supporting blob - floating across center
                val blueOffsetX = size.width * (0.5f + 0.3f * cos((phase3 + 3.4).toDouble()).toFloat())
                val blueOffsetY = size.height * (0.5f - 0.3f * sin((phase1 + 2.0).toDouble()).toFloat())
                drawCircle(
                    color = BlueAccent.copy(alpha = 0.5f),
                    center = Offset(blueOffsetX, blueOffsetY),
                    radius = size.width * 0.65f
                )
            }
        }
        
        // Micro-crystalline Frosted Glass Shimmer Grain overlaid on top
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spaceGridSize = size.width / 12f
            for (i in 1..12) {
                val posX = i * spaceGridSize
                for (j in 1..20) {
                    // Star points
                    if ((i + j) % 7 == 0) {
                        val posY = j * spaceGridSize * 1.5f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f),
                            radius = 2.0f,
                            center = Offset(posX, posY)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// GlassCard — High-index physical liquid glass card
// Leverages light transmission gradients, double highlights, and sweeping sheen sweeps
// ============================================================
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderAlpha: Float = 0.22f,
    testTagId: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glass_card_sheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6500,
                delayMillis = 1500,
                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) // CSS-like ease curve
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_progress"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(clickModifier)
            .testTag(testTagId ?: "glass_card")
    ) {
        // LAYER 1: Frosted Glass Background with backdrop-filter: blur simulation
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            renderEffect = RenderEffect.createBlurEffect(
                                25f, 25f, Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } catch (e: Throwable) {
                            // Graceful fallback if blur creation fails
                        }
                    }
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    )
                )
        )

        // LAYER 2: Double-beveled border highlights and animated liquid glare sheen
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = borderAlpha),
                            Color.White.copy(alpha = borderAlpha * 0.45f),
                            Color.White.copy(alpha = 0.0f),
                            Color.White.copy(alpha = borderAlpha * 0.25f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1f, 1f)
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .drawBehind {
                    // Internal depth reflection edge
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.04f),
                        size = size,
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                }
                .drawWithContent {
                    drawContent()
                    val sheenX = size.width * sheenProgress
                    val sheenWidth = size.width * 0.45f
                    
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.01f),
                                Color.White.copy(alpha = 0.11f),
                                Color.White.copy(alpha = 0.16f), // Peak gloss shine
                                Color.White.copy(alpha = 0.11f),
                                Color.White.copy(alpha = 0.01f),
                                Color.White.copy(alpha = 0.0f)
                            ),
                            start = Offset(sheenX - sheenWidth, -size.height * 0.2f),
                            end = Offset(sheenX + sheenWidth * 0.3f, size.height * 1.2f)
                        ),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                        size = size
                    )
                }
        )

        // LAYER 3: Razor-sharp foreground child content drawer
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

// ============================================================
// GradientButton — Premium, high-index reactive liquid glass button
// Leverages light transmission gradients, tactile response scaling, glowing background, & sheen sweeps
// ============================================================
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTagId: String = "gradient_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press scale down
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
        label = "button_scale"
    )

    // Vibrant background glow state animates with clicks
    val glowOpacity by animateFloatAsState(
        targetValue = if (!enabled) 0.0f else if (isPressed) 0.45f else 0.25f,
        animationSpec = tween(200),
        label = "button_glow"
    )

    // Gloss sheen sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "button_sheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4800,
                delayMillis = 800,
                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_progress"
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .testTag(testTagId),
        contentAlignment = Alignment.Center
    ) {
        // Under-bezel neon light back-diffusion aura (Cyan-Purple glow radiating outwards)
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (enabled) {
                // Glow 1: Cyan back-glow on the left top edge
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = glowOpacity * 0.7f), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.5f),
                        radius = size.width * 0.6f
                    )
                )
                // Glow 2: Purple-pink back-glow on the right bottom edge
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPink.copy(alpha = glowOpacity * 0.5f), Color.Transparent),
                        center = Offset(size.width * 0.75f, size.height * 0.5f),
                        radius = size.width * 0.6f
                    )
                )
            }
        }

        // Frosted highly refractive transmission body plate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (enabled) {
                            listOf(
                                Color.White.copy(alpha = 0.24f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.06f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                // Hand-polished double beveled glossy highlights border (top-left gather to bottom-right shadow)
                .border(
                    width = 1.25.dp,
                    brush = Brush.linearGradient(
                        colors = if (enabled) {
                            listOf(
                                Color.White.copy(alpha = 0.45f), // Shimmering top-left
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f)  // Soft reflections bottom-right
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(1f, 1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                // Glare sweep (simulates a liquid glass surface capturing light)
                .drawWithContent {
                    drawContent()
                    if (enabled) {
                        val sheenX = size.width * sheenProgress
                        val sheenWidth = size.width * 0.35f
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.0f),
                                    Color.White.copy(alpha = 0.03f),
                                    Color.White.copy(alpha = 0.18f), // High specular liquid shine
                                    Color.White.copy(alpha = 0.24f), // Apex specular rim
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.03f),
                                    Color.White.copy(alpha = 0.0f)
                                ),
                                start = Offset(sheenX - sheenWidth, -size.height * 0.2f),
                                end = Offset(sheenX + sheenWidth * 0.2f, size.height * 1.2f)
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                            size = size
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Text styled with premium high-contrast and a subtle white backing glow
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 15.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else TextMuted,
                modifier = Modifier.drawBehind {
                    if (enabled) {
                        // Soft text drop shadow to boost high-index refraction legibility on moving background orbs
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = size.width * 0.4f,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
            )
        }
    }
}

// ============================================================
// LiquidPandaSphere — Vector panda illustration drawn in Compose Canvas
// Pulses and breathes on its own utilizing smooth spring/sin loops
// ============================================================
@Composable
fun LiquidPandaSphere(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isThinking: Boolean = false,
    textIndicator: String = "PANDA"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "panda_anim")

    // Slow, natural breathing cycle (scales and moves elements gently)
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Vibrant glow index for cyan active rings
    val ringGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_glow"
    )

    // Continuous spin for search indicators or thinking loops
    val spinnerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spins"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.4f * breatheScale

            // Draw outer neon halo rings
            for (i in 1..2) {
                val ringRadius = baseRadius + (i * 12.dp.toPx())
                val arcPulse = if (isListening) ringGlowPulse * 1.2f else ringGlowPulse
                drawCircle(
                    color = CyanGlow.copy(alpha = 0.08f * arcPulse / i),
                    radius = ringRadius + 4.dp.toPx(),
                    center = center
                )
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.2f), PurpleAccent.copy(alpha = 0.05f), Color.Transparent)
                    ),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Draw Thinking Spinning particle
            if (isThinking) {
                val particleRadius = baseRadius + 15.dp.toPx()
                val angleRad = Math.toRadians(spinnerRotation.toDouble())
                val px = center.x + particleRadius * cos(angleRad).toFloat()
                val py = center.y + particleRadius * sin(angleRad).toFloat()
                drawCircle(
                    color = CyanGlow,
                    radius = 4.dp.toPx(),
                    center = Offset(px, py)
                )
                // Draw a secondary particle
                val px2 = center.x + particleRadius * cos(angleRad + Math.PI).toFloat()
                val py2 = center.y + particleRadius * sin(angleRad + Math.PI).toFloat()
                drawCircle(
                    color = PurpleAccent,
                    radius = 4.dp.toPx(),
                    center = Offset(px2, py2)
                )
            }

            // Inner Space Aura container
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NavyDark, DeepSpace),
                    center = center,
                    radius = baseRadius
                )
            )

            // Inner glowing rim
            drawCircle(
                color = CyanGlow.copy(alpha = 0.15f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // --------------------------------------------------------
            // PANDA ILLUSTRATION (Drawn with primitive vector shapes)
            // --------------------------------------------------------
            val pandaCenter = Offset(center.x, center.y + 6.dp.toPx())
            val faceRadiusX = baseRadius * 0.65f
            val faceRadiusY = baseRadius * 0.58f

            // 1. Ears (Drawn as dark circular pods with neon purple glow cores)
            val leftEarCH = Offset(pandaCenter.x - faceRadiusX * 0.72f, pandaCenter.y - faceRadiusY * 0.85f)
            val rightEarCH = Offset(pandaCenter.x + faceRadiusX * 0.72f, pandaCenter.y - faceRadiusY * 0.85f)
            val earRadius = faceRadiusX * 0.35f

            // Ear outer black pods
            drawCircle(color = Color(0xFF1E2640), radius = earRadius, center = leftEarCH)
            drawCircle(color = Color(0xFF1E2640), radius = earRadius, center = rightEarCH)

            // Ear inner purple cores
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PurpleAccent.copy(alpha = 0.7f), Color.Transparent),
                    center = leftEarCH,
                    radius = earRadius * 0.8f
                ),
                radius = earRadius * 0.8f,
                center = leftEarCH
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PurpleAccent.copy(alpha = 0.7f), Color.Transparent),
                    center = rightEarCH,
                    radius = earRadius * 0.8f
                ),
                radius = earRadius * 0.8f,
                center = rightEarCH
            )

            // 2. Panda White Head Base (represented as a gorgeous slightly translucent glossy capsule)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(pandaCenter.x - faceRadiusX, pandaCenter.y - faceRadiusY),
                size = Size(faceRadiusX * 2f, faceRadiusY * 2f),
                cornerRadius = CornerRadius(faceRadiusX * 0.82f, faceRadiusY * 0.82f)
            )

            // Gloss outline on panda face
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(pandaCenter.x - faceRadiusX, pandaCenter.y - faceRadiusY),
                size = Size(faceRadiusX * 2f, faceRadiusY * 2f),
                cornerRadius = CornerRadius(faceRadiusX * 0.82f, faceRadiusY * 0.82f),
                style = Stroke(width = 1.dp.toPx())
            )

            // 3. Iconic Black Eye Patches (Angled dark ovals)
            val patchW = faceRadiusX * 0.42f
            val patchH = faceRadiusY * 0.52f
            val leftPatchTL = Offset(pandaCenter.x - faceRadiusX * 0.58f, pandaCenter.y - faceRadiusY * 0.28f)
            val rightPatchTL = Offset(pandaCenter.x + faceRadiusX * 0.16f, pandaCenter.y - faceRadiusY * 0.28f)

            // Draw eye patch ovals
            drawRoundRect(
                color = Color(0xFF1E2640),
                topLeft = leftPatchTL,
                size = Size(patchW, patchH),
                cornerRadius = CornerRadius(patchW * 0.5f, patchH * 0.5f)
            )
            drawRoundRect(
                color = Color(0xFF1E2640),
                topLeft = rightPatchTL,
                size = Size(patchW, patchH),
                cornerRadius = CornerRadius(patchW * 0.5f, patchH * 0.5f)
            )

            // 4. Glowing Cybernetic Cyan Eyes (Glowing dots inside dark patches)
            val blinkCoeff = if (breatheScale < 0.98f) 0.1f else 1.0f  // Blinking animation
            val leftEyeCenter = Offset(leftPatchTL.x + patchW * 0.55f, leftPatchTL.y + patchH * 0.45f)
            val rightEyeCenter = Offset(rightPatchTL.x + patchW * 0.45f, rightPatchTL.y + patchH * 0.45f)
            val eyeRadius = 6.dp.toPx()

            // Outer cyan glowing ring
            drawCircle(
                color = CyanGlow.copy(alpha = 0.35f),
                radius = eyeRadius * 1.8f * blinkCoeff,
                center = leftEyeCenter
            )
            drawCircle(
                color = CyanGlow.copy(alpha = 0.35f),
                radius = eyeRadius * 1.8f * blinkCoeff,
                center = rightEyeCenter
            )

            // Central neon dot
            drawCircle(
                color = CyanGlow,
                radius = eyeRadius * blinkCoeff,
                center = leftEyeCenter
            )
            drawCircle(
                color = CyanGlow,
                radius = eyeRadius * blinkCoeff,
                center = rightEyeCenter
            )

            // Small eye reflections (cute white shine)
            if (blinkCoeff > 0.5f) {
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(leftEyeCenter.x - 2.dp.toPx(), leftEyeCenter.y - 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(rightEyeCenter.x - 2.dp.toPx(), rightEyeCenter.y - 2.dp.toPx())
                )
            }

            // 5. Snout (cute triangular cyber nose)
            val noseW = faceRadiusX * 0.18f
            val noseH = faceRadiusY * 0.12f
            val noseCenter = Offset(pandaCenter.x, pandaCenter.y + faceRadiusY * 0.15f)

            drawRoundRect(
                color = Color(0xFF0F1424),
                topLeft = Offset(noseCenter.x - noseW / 2f, noseCenter.y - noseH / 2f),
                size = Size(noseW, noseH),
                cornerRadius = CornerRadius(noseW * 0.4f, noseH * 0.4f)
            )

            // 6. Cybernetic Mouth Line (drawn as simple clean neon line)
            drawArc(
                color = Color(0xFF1E2640).copy(alpha = 0.6f),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(noseCenter.x - noseW * 0.8f, noseCenter.y + 4.dp.toPx()),
                size = Size(noseW * 1.6f, 8.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Small rosy cyber cheeks
            drawCircle(
                color = NeonPink.copy(alpha = 0.15f),
                radius = faceRadiusX * 0.15f,
                center = Offset(pandaCenter.x - faceRadiusX * 0.65f, pandaCenter.y + faceRadiusY * 0.12f)
            )
            drawCircle(
                color = NeonPink.copy(alpha = 0.15f),
                radius = faceRadiusX * 0.15f,
                center = Offset(pandaCenter.x + faceRadiusX * 0.65f, pandaCenter.y + faceRadiusY * 0.12f)
            )
        }

        // Foreground textual label overlay mimicking premium subtext branding in circles
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassWhite)
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = textIndicator,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CyanGlow,
                letterSpacing = 2.sp
            )
        }
    }
}

// ============================================================
// GlassDivider — Subtly frosted divider
// ============================================================
@Composable
fun GlassDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0f)
                    )
                )
            )
    )
}

// ============================================================
// FrostedGlassContainer — Reusable frosted container for chat/input
// ============================================================
@Composable
fun FrostedGlassContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .background(brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.05f)
                )
            ))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            content = content
        )
    }
}

// ============================================================
// MediumLiquidGlassCard — Premium "Medium Transparent Liquid Glass"
// Features high-index refraction, double beveled edge border, custom back-aura gloss,
// and moving specular sheen sweep. Highly optimized for medium transparency.
// ============================================================
@Composable
fun MediumLiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderAlpha: Float = 0.28f,
    blurAlphaStart: Float = 0.16f,
    blurAlphaEnd: Float = 0.05f,
    glowColor: Color = CyanGlow,
    enableGlow: Boolean = true,
    testTagId: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    val infiniteTransition = rememberInfiniteTransition(label = "medium_glass_sheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 2.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7000,
                delayMillis = 2000,
                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(clickModifier)
            .testTag(testTagId ?: "medium_liquid_glass_card")
    ) {
        // LAYER 1: Frosted Glass Background with backdrop-filter: blur simulation
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            renderEffect = RenderEffect.createBlurEffect(
                                30f, 30f, Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } catch (e: Throwable) {
                            // Graceful fallback if blur creation fails
                        }
                    }
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = blurAlphaStart),
                            Color.White.copy(alpha = blurAlphaEnd)
                        )
                    )
                )
        )

        // LAYER 2: Double-beveled border highlights, ambient backglows, and animated sheen
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    if (enableGlow) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(glowColor.copy(alpha = 0.06f), Color.Transparent),
                                center = Offset(size.width * 0.15f, size.height * 0.15f),
                                radius = size.width * 0.9f
                            ),
                            size = size,
                            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                        )
                    }
                }
                .border(
                    width = 1.25.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = borderAlpha),
                            Color.White.copy(alpha = borderAlpha * 0.4f),
                            Color.Transparent,
                            Color.White.copy(alpha = borderAlpha * 0.25f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1f, 1f)
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .drawWithContent {
                    drawContent()
                    val sheenX = size.width * sheenProgress
                    val sheenWidth = size.width * 0.4f
                    
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.02f),
                                Color.White.copy(alpha = 0.14f), // Apex of glass sheen glare
                                Color.White.copy(alpha = 0.22f), // Strong specular reflection line
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.02f),
                                Color.White.copy(alpha = 0.0f)
                            ),
                            start = Offset(sheenX - sheenWidth, -size.height * 0.1f),
                            end = Offset(sheenX + sheenWidth * 0.2f, size.height * 1.1f)
                        ),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                        size = size
                    )
                }
        )

        // LAYER 3: Razor-sharp foreground child content drawer
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

// ============================================================
// LiquidGlassChatCard — Custom premium container to frame the entire chat feed
// Houses conversational feeds, active partner indicator, scroll states, and layouts.
// ============================================================
@Composable
fun LiquidGlassChatCard(
    modifier: Modifier = Modifier,
    title: String = "Panda Assistant",
    statusText: String = "Online",
    statusColor: Color = StatusGreen,
    onHeaderActionClick: (() -> Unit)? = null,
    headerIcon: ImageVector = Icons.Default.DeleteSweep,
    headerActionDescription: String = "Clear Conversations",
    content: @Composable ColumnScope.() -> Unit
) {
    MediumLiquidGlassCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        glowColor = CyanGlow.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Interactive Glass Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar badge inside mini liquid pod
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, CyanGlow.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐼", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Live breathing indicator dot
                        val transition = rememberInfiniteTransition(label = "chat_active_breathing")
                        val breatheAlpha by transition.animateFloat(
                            initialValue = 0.35f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "breathe"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = breatheAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                if (onHeaderActionClick != null) {
                    IconButton(
                        onClick = onHeaderActionClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(0.5.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = headerActionDescription,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // High-refraction separating divider
            GlassDivider(modifier = Modifier.fillMaxWidth())

            // Conversational content payload
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

// ============================================================
// LiquidGlassHelperCard — Dedicated visual panel to house assistive/secondary widgets
// Features left accent aesthetic strip, high contrast titles, and responsive glass button highlights.
// ============================================================
@Composable
fun LiquidGlassHelperCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    accentColor: Color = PurpleAccent,
    icon: ImageVector? = null,
    testTagId: String? = null,
    onClick: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null,
    secondaryActionIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    MediumLiquidGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        glowColor = accentColor,
        testTagId = testTagId,
        onClick = onClick
    ) {
        // Vertical accent glow pill on the left edge
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.2f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(0.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (onSecondaryAction != null && secondaryActionIcon != null) {
                    IconButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = secondaryActionIcon,
                            contentDescription = "Quick Action",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Decorative Horizontal glass separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Body content injection
            content()
        }
    }
}

// ============================================================
// LiquidGlassButton — Highly stylized animated transparent button
// ============================================================
@Composable
fun LiquidGlassButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    isActive: Boolean = false,
    accentColor: Color = CyanGlow
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btn_shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Frosted Background Blur Simulator
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            renderEffect = RenderEffect.createBlurEffect(
                                20f, 20f, Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } catch (e: Throwable) {
                            // Ignored
                        }
                    }
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // LAYER 2: Borders, back-glow and liquid shimmer sheen
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .drawWithContent {
                    drawContent()
                    val sweepWidth = size.width * 0.35f
                    val startX = size.width * shimmerX
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            start = Offset(startX - sweepWidth, 0f),
                            end = Offset(startX + sweepWidth, size.height)
                        ),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )

                    if (isActive) {
                        drawRoundRect(
                            color = accentColor.copy(alpha = 0.15f),
                            size = size,
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                        )
                    }
                }
        )

        // LAYER 3: Razor-sharp foreground elements
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) accentColor else TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) accentColor else TextPrimary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ============================================================
// LiquidGlassInputField — Bottom-aligned text input
// ============================================================
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Ask Panda anything...",
    onSend: () -> Unit,
    onMicClick: () -> Unit = {},
    showMic: Boolean = true,
    isListening: Boolean = false,
    focusManager: androidx.compose.ui.focus.FocusManager? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(targetValue = if (isFocused) 0.6f else 0.0f, label = "glowAlpha")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        // LAYER 1: Dynamic blurred background
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            renderEffect = RenderEffect.createBlurEffect(
                                30f, 30f, Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } catch (e: Throwable) {
                            // Ignored
                        }
                    }
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // LAYER 2: Focus Glow and Borders
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    // Outer glow when focused
                    if (glowAlpha > 0f) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(CyanGlow.copy(alpha = glowAlpha * 0.4f), Color.Transparent),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width * 0.8f
                            ),
                            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                            size = size
                        )
                    }
                }
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            if (isFocused) CyanGlow.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                            if (isFocused) CyanGlow.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                            Color.Transparent,
                            if (isFocused) CyanGlow.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        // LAYER 3: Input Field and Action Buttons
        Row(
            modifier = Modifier.matchParentSize().padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Attachment action */ }, modifier = Modifier.size(36.dp)) {
                androidx.compose.material3.Icon(Icons.Default.AddCircleOutline, null, tint = TextMuted)
            }
            
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 15.sp),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(CyanGlow),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = {
                    if (value.isNotBlank()) {
                        onSend()
                    }
                }),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholderText,
                            style = LocalTextStyle.current.copy(color = TextMuted, fontSize = 15.sp)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing Icons (Clear, Send, Mic)
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(36.dp)) {
                    androidx.compose.material3.Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyanGlow.copy(alpha = 0.15f))
                        .clickable {
                            if (value.isNotBlank()) onSend()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(Icons.Default.Send, null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                }
            } else if (showMic) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) CyanGlow.copy(alpha = 0.3f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .border(
                            0.5.dp, 
                            if (isListening) CyanGlow else GlassBorder, 
                            CircleShape
                        )
                        .clickable(onClick = onMicClick),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) CyanGlow else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
