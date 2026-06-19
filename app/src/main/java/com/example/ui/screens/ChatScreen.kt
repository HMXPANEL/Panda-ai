package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AiState
import com.example.data.ChatMessage
import com.example.ui.PandaViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: PandaViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var chatInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Automatically scroll to the end on new message insertions
    LaunchedEffect(messages.size, aiState) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("chat_screen_content")
    ) {
        LiquidGlassChatCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            title = "Panda AI",
            statusText = "Online",
            statusColor = StatusGreen,
            onHeaderActionClick = { viewModel.clearChat() }
        ) {
            // --- 2. Live Conversations Column ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ChatBubbleRow(message = message, userName = userName)
                }

                // Thinking typing overlay
                if (aiState == AiState.Thinking) {
                    item {
                        ThinkingBubbleRow()
                    }
                }
            }
        }

        // --- 3. Interactive Input Line ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 8.dp)
        ) {
             LiquidGlassInputField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholderText = "Message Panda...",
                isListening = aiState == AiState.Listening,
                focusManager = focusManager,
                onSend = {
                    viewModel.sendMessage(chatInput)
                    chatInput = ""
                    focusManager.clearFocus()
                },
                onMicClick = {
                    if (aiState == AiState.Idle) {
                        viewModel.startVoiceListening(context)
                    }
                },
                onAttachmentClick = {
                    viewModel.sendMessage("Attachment feature - select file to share")
                }
            )
        }
    }
}

@Composable
fun ChatBubbleRow(message: ChatMessage, userName: String) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Small Panda icon badge on left
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.5.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🐼", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Bubble Box
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        // User gradient: indigo to purple glass
                        Brush.linearGradient(
                            colors = listOf(
                                GradientStart.copy(alpha = 0.72f),
                                GradientEnd.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        // Agent: Frosted glass translucent
                        Brush.linearGradient(
                            colors = listOf(
                                GlassWhite,
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.White.copy(alpha = 0.25f) else GlassBorder,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Column {
                // Name handle
                Text(
                    text = if (isUser) userName else "Panda Agent",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color.White.copy(alpha = 0.8f) else CyanGlow,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))

                // Content
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Small User icon badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GradientStart.copy(alpha = 0.2f))
                    .border(0.5.dp, GradientStart, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ThinkingBubbleRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🐼", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp
                    )
                )
                .background(GlassWhite)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Panda typing", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                // Small bouncing dots
                val infiniteTransition = rememberInfiniteTransition(label = "dots")
                for (i in 0..2) {
                    val dotY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(delayMillis = i * 150, durationMillis = 400, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_bounce"
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dotY.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(CyanGlow)
                    )
                }
            }
        }
    }
}
