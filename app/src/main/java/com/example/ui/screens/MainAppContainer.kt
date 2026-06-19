package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BottomTab
import com.example.ui.PandaViewModel
import com.example.ui.components.DynamicBackground
import com.example.ui.components.MediumLiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MainAppContainer(viewModel: PandaViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val backgroundStyle by viewModel.backgroundStyle.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_container_root")
    ) {
        // Space blobs back-depth on all pages
        DynamicBackground(style = backgroundStyle)

        Scaffold(
            containerColor = Color.Transparent, // Let the background glass orbs flow underneath!
            bottomBar = {
                FloatingGlassNavigationBar(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .statusBarsPadding()
            ) {
                // Crossfade animation for tab transitions
                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(350),
                    label = "tab_transitions"
                ) { tab ->
                    when (tab) {
                        BottomTab.Home -> HomeScreen(viewModel = viewModel)
                        BottomTab.Chat -> ChatScreen(viewModel = viewModel)
                        BottomTab.Tools -> ToolsScreen(viewModel = viewModel)
                        BottomTab.Settings -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingGlassNavigationBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .navigationBarsPadding() // Keep touch zones safe above system gestures
            .testTag("floating_glass_nav")
    ) {
        MediumLiquidGlassCard(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            cornerRadius = 32.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    tab = BottomTab.Home,
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = selectedTab == BottomTab.Home,
                    onClick = { onTabSelected(BottomTab.Home) }
                )
                NavBarItem(
                    tab = BottomTab.Chat,
                    icon = Icons.Default.Chat,
                    label = "Chat",
                    isSelected = selectedTab == BottomTab.Chat,
                    onClick = { onTabSelected(BottomTab.Chat) }
                )
                NavBarItem(
                    tab = BottomTab.Tools,
                    icon = Icons.Default.Build,
                    label = "Tools",
                    isSelected = selectedTab == BottomTab.Tools,
                    onClick = { onTabSelected(BottomTab.Tools) }
                )
                NavBarItem(
                    tab = BottomTab.Settings,
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = selectedTab == BottomTab.Settings,
                    onClick = { onTabSelected(BottomTab.Settings) }
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    tab: BottomTab,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconFallback: ImageVector? = null
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_item_${label.lowercase()}"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing pill background under active item
        Box(
            modifier = Modifier
                .height(30.dp)
                .width(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) CyanGlow.copy(alpha = 0.15f)
                    else Color.Transparent
                )
                .border(
                    width = if (isSelected) 0.5.dp else 0.dp,
                    color = if (isSelected) CyanGlow.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconFallback ?: icon,
                contentDescription = label,
                tint = if (isSelected) CyanGlow else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CyanGlow else TextMuted,
            letterSpacing = 0.5.sp
        )
    }
}
