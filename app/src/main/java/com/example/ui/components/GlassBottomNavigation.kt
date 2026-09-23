package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalGlassColors

enum class AppDestination(val route: String, val label: String) {
    HOME("home", "Home"),
    QUIZZES("quizzes", "Quizzes"),
    STATS("stats", "Stats"),
    SETTINGS("settings", "Settings")
}

@Composable
fun GlassBottomNavigation(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(16.dp, shape, ambientColor = Color(0x30000000), spotColor = Color(0x40000000))
                .clip(shape)
                .background(Color(0x400D1C34))
                .border(BorderStroke(1.dp, Color(0x55FFFFFF)), shape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDestination.entries.forEach { dest ->
                    val isSelected = dest == currentDestination
                    val icon = when (dest) {
                        AppDestination.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        AppDestination.QUIZZES -> if (isSelected) Icons.Filled.LibraryBooks else Icons.Outlined.LibraryBooks
                        AppDestination.STATS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                        AppDestination.SETTINGS -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                    }

                    val tint by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF54A0FF) else Color(0xB3FFFFFF),
                        animationSpec = tween(durationMillis = 200),
                        label = "nav_tint"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                                onClick = { onDestinationSelected(dest) }
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = dest.label,
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = dest.label,
                            color = tint,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
