package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScreenState
import com.example.ui.PandaViewModel
import com.example.ui.components.AnimatedBackgroundOrbs
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassDivider
import com.example.ui.components.LiquidGlassButton
import com.example.ui.theme.*

@Composable
fun PermissionsScreen(viewModel: PandaViewModel) {
    val accessibility by viewModel.isAccessibilityGranted.collectAsState()
    val overlay by viewModel.isOverlayGranted.collectAsState()
    val mic by viewModel.isMicGranted.collectAsState()
    val notifications by viewModel.isNotificationsGranted.collectAsState()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleMic(true) else viewModel.toggleMic(false)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleNotifications(true) else viewModel.toggleNotifications(false)
    }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.toggleOverlay(Settings.canDrawOverlays(context))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("permissions_screen_root")
    ) {
        AnimatedBackgroundOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp)
            ) {
                Text(
                    text = "Grant Permissions",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Panda needs a few permissions\nto help you better.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    lineHeight = 22.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        PermissionRow(
                            icon = Icons.Default.AccessibilityNew,
                            title = "Accessibility Service",
                            subtitle = "To interact with apps",
                            isGranted = accessibility,
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            }
                        )

                        GlassDivider()

                        PermissionRow(
                            icon = Icons.Default.Layers,
                            title = "Overlay Permission",
                            subtitle = "To show floating assistant",
                            isGranted = overlay,
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                overlaySettingsLauncher.launch(intent)
                            }
                        )

                        GlassDivider()

                        PermissionRow(
                            icon = Icons.Default.Mic,
                            title = "Microphone",
                            subtitle = "For voice commands",
                            isGranted = mic,
                            onClick = {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )

                        GlassDivider()

                        PermissionRow(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "To read and manage alerts",
                            isGranted = notifications,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleNotifications(true)
                                }
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LiquidGlassButton(
                    text = "Continue",
                    onClick = {
                        viewModel.navigateTo(ScreenState.MainApp)
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isGranted) StatusGreen.copy(alpha = 0.15f)
                    else PurpleAccent.copy(alpha = 0.12f)
                )
                .border(
                    width = 1.dp,
                    color = if (isGranted) StatusGreen.copy(alpha = 0.35f) else GlassBorder,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) StatusGreen else CyanGlow,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isGranted) StatusGreen.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.05f)
                )
                .border(
                    width = 1.dp,
                    color = if (isGranted) StatusGreen else GlassBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = StatusGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
