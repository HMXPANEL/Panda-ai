package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Memory
import com.example.ui.PandaViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassDivider
import com.example.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MemoriesScreen(viewModel: PandaViewModel) {
    val memoriesList by viewModel.memories.collectAsState()

    var activeCategoryFilter by remember { mutableStateOf("All") }
    var entryText by remember { mutableStateOf("") }
    var showAddRow by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMemoryDetail by remember { mutableStateOf<Memory?>(null) }

    // Filter list
    val filteredMemories = remember(memoriesList, activeCategoryFilter, searchQuery) {
        var list = if (activeCategoryFilter == "All") {
            memoriesList
        } else {
            memoriesList.filter { it.category.equals(activeCategoryFilter, ignoreCase = true) }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.content.lowercase().contains(searchQuery.lowercase()) }
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("memories_screen_content")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with dynamic Add "+" trigger
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp).clickable { /* Go Back */ }
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Memories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Panda remembers",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            
            // Glass Plus Circle button matching Screen 11 header
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(0.5.dp, GlassBorder, CircleShape)
                    .clickable { showAddRow = !showAddRow },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (showAddRow) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Toggle Add Memory",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic addition row
        if (showAddRow) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = entryText,
                        onValueChange = { entryText = it },
                        placeholder = { Text("Panda remembers that you...", fontSize = 13.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = CyanGlow
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Choose Category Selector inside small glass dropdown or trigger save
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanGlow.copy(alpha = 0.15f))
                            .border(1.dp, CyanGlow.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
                                if (entryText.isNotBlank()) {
                                    val cat = if (activeCategoryFilter == "All") "Important" else activeCategoryFilter
                                    viewModel.addMemory(entryText, cat)
                                    entryText = ""
                                    showAddRow = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = CyanGlow, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Category Filter Row in image (All, Important, Conversations)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("All", "Important", "Conversations").forEach { category ->
                val isSelected = category == activeCategoryFilter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) GradientStart.copy(alpha = 0.22f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CyanGlow else GlassBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { activeCategoryFilter = category }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanGlow else TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Memories List
        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No memories synced in this category.\nTap + above to seed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredMemories) { memory ->
                    MemoryItemRow(memory = memory, onClick = { showMemoryDetail = memory })
                }
            }
        }

        // Search Box (Bottom)
        Spacer(modifier = Modifier.height(16.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
            cornerRadius = 28.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                androidx.compose.material3.TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search memories...", color = TextMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(start = 12.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 14.sp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedIndicatorColor = CyanGlow,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = CyanGlow,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp).padding(end = 12.dp)
                )
            }
        }
    }

    // Memory Detail Dialog
    showMemoryDetail?.let { memory ->
        AlertDialog(
            onDismissRequest = { showMemoryDetail = null },
            containerColor = NavyDark,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Memory Detail", fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = { showMemoryDetail = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = memory.content,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Text("Category: ", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text(memory.category, color = TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy - hh:mm a").withZone(ZoneId.systemDefault()) }
                    val dateString = formatter.format(Instant.ofEpochMilli(memory.timestamp))
                    Row {
                        Text("Created: ", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text(dateString, color = TextSecondary, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        viewModel.deleteMemory(memory)
                        showMemoryDetail = null
                    }) {
                        Text("Delete", color = StatusRed)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showMemoryDetail = null }) {
                        Text("Close", color = CyanGlow)
                    }
                }
            }
        )
    }
}

@Composable
fun MemoryItemRow(memory: Memory, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy - hh:mm a").withZone(ZoneId.systemDefault()) }
    val dateString = remember(memory.timestamp) { formatter.format(Instant.ofEpochMilli(memory.timestamp)) }

    GlassCard(
        cornerRadius = 18.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Memory icon tag
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(0.5.dp, GlassBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(memory.category) {
                        "Conversations" -> Icons.Default.ChatBubbleOutline
                        "Important" -> Icons.Default.StarOutline
                        else -> Icons.Default.FavoriteBorder
                    },
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memory.content,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
