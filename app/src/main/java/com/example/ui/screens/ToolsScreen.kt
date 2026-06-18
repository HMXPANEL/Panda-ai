package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.ui.BottomTab
import com.example.ui.PandaViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ToolsScreen(viewModel: PandaViewModel) {
    val context = LocalContext.current

    // Local interactive states for device toggles
    var isWifiOn by remember { mutableStateOf(true) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isMusicPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("tools_screen_content")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "Tools & Actions",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Panda can perform custom actions",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tools Grid (3 columns for cute alignment like Screen 10)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                ToolGridCard(
                    icon = Icons.Default.Phone,
                    title = "Call",
                    iconColor = BlueAccent,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Call main contact 📞")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.Comment,
                    title = "Message",
                    iconColor = CyanGlow,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Compose quick message")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.Email,
                    title = "Email",
                    iconColor = PurpleAccent,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Open email inbox")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }

            item {
                ToolGridCard(
                    icon = Icons.Default.Apps,
                    title = "Open App",
                    iconColor = SoftAmber,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Open Instagram and search for tesla cars")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.ScreenSearchDesktop,
                    title = "Search Web",
                    iconColor = CyanGlow,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Search Web: Best Tesla models")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Screen Capture",
                    iconColor = NeonPink,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Take screenshot of active page")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }

            item {
                ToolGridCard(
                    icon = if (isMusicPlaying) Icons.Default.PlayArrow else Icons.Default.MusicNote,
                    title = if (isMusicPlaying) "Playing" else "Music",
                    iconColor = PurpleAccent,
                    isActive = isMusicPlaying,
                    onClick = {
                        isMusicPlaying = !isMusicPlaying
                        viewModel.sendMessage("Toggle music player: ${if (isMusicPlaying) "Active" else "Idle"}")
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.Alarm,
                    title = "Alarm",
                    iconColor = SoftAmber,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("Set alarm for 7:00 AM ⏰")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.Wifi,
                    title = if (isWifiOn) "Wi-Fi On" else "Wi-Fi Off",
                    iconColor = BlueAccent,
                    isActive = isWifiOn,
                    onClick = {
                        isWifiOn = !isWifiOn
                        viewModel.sendMessage("Toggle Wi-Fi: ${if (isWifiOn) "Active" else "Deactive"}")
                    }
                )
            }

            item {
                ToolGridCard(
                    icon = Icons.Default.Bluetooth,
                    title = if (isBluetoothOn) "BT On" else "BT Off",
                    iconColor = CyanGlow,
                    isActive = isBluetoothOn,
                    onClick = {
                        isBluetoothOn = !isBluetoothOn
                        viewModel.sendMessage("Toggle Bluetooth: ${if (isBluetoothOn) "Active" else "Deactive"}")
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.FlashlightOn,
                    title = if (isFlashlightOn) "Torch On" else "Torch Off",
                    iconColor = if (isFlashlightOn) Color.Yellow else TextMuted,
                    isActive = isFlashlightOn,
                    onClick = {
                        isFlashlightOn = !isFlashlightOn
                        com.example.ui.SystemIntegrations.toggleFlashlight(context)
                    }
                )
            }
            item {
                ToolGridCard(
                    icon = Icons.Default.MoreHoriz,
                    title = "More",
                    iconColor = Color.White,
                    isActive = false,
                    onClick = {
                        viewModel.sendMessage("What additional automations can you perform?")
                        viewModel.selectTab(BottomTab.Chat)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reusable Helper Card illustrating active assistant presets
        LiquidGlassHelperCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            title = "Active Sub-Systems",
            subtitle = "Panda AI continuous monitor status",
            accentColor = CyanGlow,
            icon = Icons.Default.SettingsSuggest
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Refraction Depth: Medium Glass",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ACTIVE",
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ToolGridCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    // Animated back highlighting
    val targetBorderColor = if (isActive) CyanGlow else Color.Transparent
    val borderAnim by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300), label = "border")

    GlassCard(
        cornerRadius = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = borderAnim,
                shape = RoundedCornerShape(18.dp)
            ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon capsule
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) iconColor.copy(alpha = 0.22f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) iconColor else GlassBorder,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) iconColor else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) CyanGlow else TextPrimary,
                maxLines = 1
            )
        }
    }
}
