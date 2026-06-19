package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.example.ui.PandaViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassDivider
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: PandaViewModel) {
    val scrollState = rememberScrollState()

    // State bindings
    val userName by viewModel.userName.collectAsState()
    val activeModel by viewModel.activeModelName.collectAsState()
    val userApiKey by viewModel.userApiKey.collectAsState()
    val isAssistantActive by viewModel.isAssistantActive.collectAsState()
    val prefVoice by viewModel.preferredVoice.collectAsState()
    val ansStyle by viewModel.answerStyle.collectAsState()
    val backgroundStyle by viewModel.backgroundStyle.collectAsState()

    // Interactive expansion toggles
    var isEditingProfile by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf(userName) }

    var isEditingApiKey by remember { mutableStateOf(false) }
    var inputKey by remember { mutableStateOf(userApiKey) }

    var showModelSelector by remember { mutableStateOf(false) }
    var showBackgroundSelector by remember { mutableStateOf(false) }
    var showVoiceSelector by remember { mutableStateOf(false) }
    var showAboutView by remember { mutableStateOf(false) }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var showPermissionsView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .testTag("settings_screen_content")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header (Matches Image 12 exactly)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp).clickable { /* Go Back */ }
            )
            Spacer(modifier = Modifier.width(36.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(end = 56.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Customize Panda",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Profile
                SettingsListRow(
                    icon = Icons.Default.Person,
                    title = "Profile",
                    iconColor = TextPrimary,
                    onClick = { isEditingProfile = !isEditingProfile }
                )

                AnimatedVisibility(visible = isEditingProfile) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("Enter name...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = GlassBorder,
                                cursorColor = CyanGlow,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanGlow.copy(alpha = 0.15f))
                                .border(1.dp, CyanGlow, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (inputName.isNotBlank()) {
                                        viewModel.updateUserName(inputName)
                                        isEditingProfile = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Save, null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                GlassDivider()

                // AI Model
                SettingsListRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI Model",
                    trailingText = if(activeModel.contains("pro")) "Gemini 2.5 Pro" else "Gemini 2.0 Flash",
                    iconColor = TextPrimary,
                    onClick = { showModelSelector = true }
                )

                GlassDivider()

                // Voice & Speech
                SettingsListRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Voice & Speech",
                    iconColor = TextPrimary,
                    onClick = { showVoiceSelector = true }
                )

                GlassDivider()

                // Appearance
                SettingsListRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    trailingText = "Liquid Glass",
                    iconColor = TextPrimary,
                    onClick = { showBackgroundSelector = true }
                )

                GlassDivider()

                // Panda Answer Style
                SettingsListRow(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "Panda Answer Style",
                    iconColor = TextPrimary,
                    onClick = {
                        val nextStyle = if (ansStyle == "Concise") "Conversational" else "Concise"
                        viewModel.setAnswerStyle(nextStyle)
                    }
                )

                GlassDivider()

                // Language
                SettingsListRow(
                    icon = Icons.Default.Language,
                    title = "Language",
                    trailingText = "English",
                    iconColor = TextPrimary,
                    onClick = { showLanguageSelector = true }
                )

                GlassDivider()

                // Permissions
                SettingsListRow(
                    icon = Icons.Default.Lock,
                    title = "Permissions",
                    iconColor = TextPrimary,
                    onClick = { showPermissionsView = true }
                )

                GlassDivider()

                // Data & Storage
                SettingsListRow(
                    icon = Icons.Default.Storage,
                    title = "Data & Storage",
                    iconColor = TextPrimary,
                    onClick = {
                        viewModel.clearMemories()
                        viewModel.clearChat()
                    }
                )
                
                GlassDivider()

                // About Panda
                SettingsListRow(
                    icon = Icons.Default.Info,
                    title = "About Panda",
                    trailingText = "Version 1.1.0",
                    iconColor = TextPrimary,
                    onClick = { showAboutView = true }
                )
            }
        }

        // Developer API Key Override (Hidden inside UI but functional)
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x33EF4444))
                .border(1.dp, StatusRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable {
                    // Purge values
                    viewModel.updateUserName("Harsh")
                    viewModel.updateUserApiKey("")
                    viewModel.clearChat()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "RESET ALL APPLICATION ENVIRONMENT",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = StatusRed,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // --- DIALOG: AI Model Selector (Step 9 ModelPickerDialog) ---
    if (showModelSelector) {
        AlertDialog(
            onDismissRequest = { showModelSelector = false },
            containerColor = NavyDark,
            title = {
                Text(
                    "Select AI Model",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val models = listOf(
                        "gemini-2.0-flash" to "Speed optimized, perfect for text & speech",
                        "gemini-2.5-pro-exp-03-25" to "Pro capability, expert reasoning"
                    )
                    models.forEach { (modelId, desc) ->
                        val isSelected = activeModel == modelId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) GradientStart.copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyanGlow else GlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.updateActiveModel(modelId)
                                    showModelSelector = false
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = modelId,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanGlow else TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    if (modelId.contains("pro")) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PurpleAccent.copy(alpha = 0.3f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("PRO", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
    }

    // --- DIALOG: Language Selector ---
    if (showLanguageSelector) {
        AlertDialog(
            onDismissRequest = { showLanguageSelector = false },
            containerColor = NavyDark,
            title = { Text("Select Language", color = TextPrimary) },
            text = {
                Column {
                    listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Korean", "Hindi").forEach { lang ->
                        TextButton(
                            onClick = {
                                // TODO: Implement language change
                                showLanguageSelector = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(lang, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageSelector = false }) {
                    Text("Close", color = CyanGlow)
                }
            }
        )
    }

    // --- DIALOG: Permissions View ---
    if (showPermissionsView) {
        AlertDialog(
            onDismissRequest = { showPermissionsView = false },
            containerColor = NavyDark,
            title = { Text("App Permissions", color = TextPrimary) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val permissions = listOf(
                        "Microphone" to "Record audio for voice commands",
                        "Notifications" to "Show alerts and reminders",
                        "Camera" to "Take photos for screen capture",
                        "Contacts" to "Access contacts for calls/messages",
                        "Calendar" to "Create and manage events",
                        "Phone" to "Make direct calls",
                        "SMS" to "Send text messages",
                        "Accessibility" to "Interact with other apps",
                        "Overlay" to "Show floating assistant",
                        "Location" to "Provide location-based services",
                        "Storage" to "Read/write files"
                    )
                    permissions.forEach { (name, desc) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(desc, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPermissionsView = false }) {
                    Text("Close", color = CyanGlow)
                }
            }
        )
    }
}
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = desc, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelSelector = false }) {
                    Text("Close", color = CyanGlow)
                }
            }
        )
    }

    // --- DIALOG: Background Animation Selector ---
    if (showBackgroundSelector) {
        AlertDialog(
            onDismissRequest = { showBackgroundSelector = false },
            containerColor = NavyDark,
            title = { Text("Select Animation", color = TextPrimary) },
            text = {
                Column {
                    com.example.ui.BackgroundStyle.values().forEach { style ->
                        TextButton(
                            onClick = {
                                viewModel.updateBackgroundStyle(style)
                                showBackgroundSelector = false
                            },
                             modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(style.name, color = if (backgroundStyle == style) CyanGlow else TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // --- DIALOG: Voice Pick selector ---
    if (showVoiceSelector) {
        AlertDialog(
            onDismissRequest = { showVoiceSelector = false },
            containerColor = NavyDark,
            title = {
                Text(
                    "Preferred Voice Config",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Kore", "Aoede", "Puck").forEach { voice ->
                        val isSelected = prefVoice == voice
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) GradientStart.copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyanGlow else GlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.setPreferredVoice(voice)
                                    showVoiceSelector = false
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$voice Voice",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) CyanGlow else TextPrimary
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = StatusGreen)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // --- DIALOG: About Details (Screen 14) ---
    if (showAboutView) {
        AlertDialog(
            onDismissRequest = { showAboutView = false },
            containerColor = NavyDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐼 About Panda", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Panda AI Agent is a high-performance personal assistant designed to fulfill user instructions safely and intelligently, utilizing a premium Liquid Glass UI/UX design.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "Version: 1.1.0\nFramework: Jetpack Compose M3\nPackage: com.aistudio.pandaagent\nRelease Year: 2026",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )

                    GlassDivider()

                    Text(
                        text = "Terms of Service",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyanGlow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://panda-ai.example.com/tos")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                LocalContext.current.startActivity(intent)
                            }
                    )
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyanGlow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://panda-ai.example.com/privacy")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                LocalContext.current.startActivity(intent)
                            }
                    )
                    Text(
                        text = "Open Source Licenses",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyanGlow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/panda-ai/licenses")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                LocalContext.current.startActivity(intent)
                            }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutView = false }) {
                    Text("Dismiss", color = CyanGlow)
                }
            }
        )
    }
}

@Composable
fun SettingsListRow(
    icon: ImageVector,
    title: String,
    trailingText: String? = null,
    iconColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f))
                .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(end = 6.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
