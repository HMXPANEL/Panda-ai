package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.AiState
import com.example.ui.BottomTab
import com.example.ui.PandaViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassDivider
import com.example.ui.components.LiquidGlassInputField
import com.example.ui.components.LiquidPandaSphere
import com.example.ui.components.MediumLiquidGlassCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(viewModel: PandaViewModel) {
    val userName by viewModel.userName.collectAsState()
    val isAssistantActive by viewModel.isAssistantActive.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var drawerState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .testTag("home_screen_content")
        ) {
            // --- 1. Custom Top Greet Bar ---
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cute menu-icon layout in glass pod
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, GlassBorder, RoundedCornerShape(10.dp))
                        .clickable { drawerState = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu drawer",
                        tint = TextPrimary
                    )
                }

            // Power button indicator
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAssistantActive) StatusGreen.copy(alpha = 0.15f)
                        else NeonPink.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isAssistantActive) StatusGreen else NeonPink.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .clickable {
                        viewModel.setAssistantActive(!isAssistantActive)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAssistantActive) Icons.Default.PowerSettingsNew else Icons.Default.PowerOff,
                    contentDescription = "Toggle Assistant",
                    tint = if (isAssistantActive) StatusGreen else NeonPink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. Main Greeting Text ---
        Text(
            text = "Hello, $userName 👋",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "How can I help you today?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Large Animated Floating Panda ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidPandaSphere(
                modifier = Modifier.size(230.dp),
                isListening = aiState == AiState.Listening,
                isThinking = aiState == AiState.Thinking,
                textIndicator = when (aiState) {
                    AiState.Listening -> "PANDA IS LISTENING..."
                    AiState.Thinking -> "PANDA IS THINKING..."
                    AiState.Idle -> "PANDA CORE ACTIVE"
                }
            )
        }

        // --- 4. Interactive Glass Search Input ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
             LiquidGlassInputField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholderText = "Ask Panda anything...",
                isListening = aiState == AiState.Listening,
                focusManager = focusManager,
                onSend = {
                    viewModel.sendMessage(textInput)
                    textInput = ""
                    focusManager.clearFocus()
                    // Route tab directly to conversation "Chat" tab!
                    viewModel.selectTab(BottomTab.Chat)
                },
                onMicClick = {
                    if (aiState == AiState.Idle) {
                        viewModel.startVoiceListening(LocalContext.current)
                    }
                },
                onAttachmentClick = {
                    viewModel.sendMessage("Attachment feature - select file to share")
                }
            )
        }

        // --- 5. Quick Trigger Cards Grid ---
        val context = LocalContext.current
        val gridSpacing = 12.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(gridSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.Phone,
                        title = "Direct Call",
                        iconColor = BlueAccent,
                        onClick = {
                            viewModel.sendMessage("Call a contact - please specify phone number")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.Message,
                        title = "Send SMS",
                        iconColor = CyanGlow,
                        onClick = {
                            viewModel.sendMessage("Send SMS - please specify phone number and message")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.Alarm,
                        title = "Set Alarm",
                        iconColor = TextPrimary,
                        onClick = {
                            viewModel.sendMessage("Set alarm - please specify time (e.g., 7:30 AM)")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.PlayArrow,
                        title = "Play/Pause",
                        iconColor = SoftAmber,
                        onClick = {
                            com.example.ui.SystemIntegrations.toggleMediaPlayback(context)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.Event,
                        title = "Add Event",
                        iconColor = NeonPink,
                        onClick = {
                            viewModel.sendMessage("Add calendar event - please specify title, description, and time")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.Map,
                        title = "Open Maps",
                        iconColor = PurpleAccent,
                        onClick = {
                            viewModel.sendMessage("Open Maps - please specify location to search")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.FlashlightOn,
                        title = "Flashlight",
                        iconColor = CyanGlow,
                        onClick = {
                            com.example.ui.SystemIntegrations.toggleFlashlight(context)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Speak",
                        iconColor = SoftAmber,
                        onClick = {
                            viewModel.sendMessage("Speak text - please specify what to say")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.Vibration,
                        title = "Vibrate",
                        iconColor = TextPrimary,
                        onClick = {
                            com.example.ui.SystemIntegrations.vibrateDevice(context)
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.ContentCopy,
                        title = "Copy Text",
                        iconColor = BlueAccent,
                        onClick = {
                            viewModel.sendMessage("Copy text to clipboard - please specify text to copy")
                            viewModel.selectTab(BottomTab.Chat)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.BatteryFull,
                        title = "Battery",
                        iconColor = NeonPink,
                        onClick = {
                            com.example.ui.SystemIntegrations.readBatteryLevel(context)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        icon = Icons.Default.VolumeUp,
                        title = "Max Volume",
                        iconColor = PurpleAccent,
                        onClick = {
                            com.example.ui.SystemIntegrations.maximizeVolume(context)
                        }
                    )
        }
    }

    // Side Drawer
    if (drawerState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { drawerState = false }
        ) {
            MediumLiquidGlassCard(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .padding(16.dp),
                cornerRadius = 24.dp,
                glowColor = CyanGlow,
                enableGlow = true
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Menu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { drawerState = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                    GlassDivider()

                    // Menu Items
                    listOf(
                        "Home" to Icons.Default.Home to BottomTab.Home,
                        "Chat" to Icons.Default.Chat to BottomTab.Chat,
                        "Tools" to Icons.Default.Build to BottomTab.Tools,
                        "Memories" to Icons.Default.Psychology to null,
                        "Settings" to Icons.Default.Settings to BottomTab.Settings
                    ).forEach { (title, icon, tab) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    drawerState = false 
                                    tab?.let { viewModel.selectTab(it) }
                                    if (title == "Memories") {
                                        // Navigate to Memories - we'll use a different approach
                                        // For now, show a message and route through chat
                                        viewModel.sendMessage("Show my memories")
                                        viewModel.selectTab(BottomTab.Chat)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    GlassDivider()

                    // User Info
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Signed in as", fontSize = 12.sp, color = TextMuted)
                        Text(userName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Text("Panda AI v1.1.0", fontSize = 11.sp, color = TextSubtle)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    GlassCard(
        cornerRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title.replace(" ", "\n"),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}
