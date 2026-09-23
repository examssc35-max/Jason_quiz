package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffold
import com.example.ui.theme.AccentColors
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    viewModel: QuizViewModel
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header matching Screen 8: "Settings"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // APPEARANCE SECTION matching Screen 8
                item {
                    SectionHeader("Appearance")
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0x30112442)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Theme Selector
                            Column {
                                Text("Theme", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("system" to "System", "dark" to "Dark", "light" to "Light").forEach { (key, label) ->
                                        val isSelected = settings.theme == key
                                        SegmentPill(
                                            text = label,
                                            isSelected = isSelected,
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.settingsRepository.updateTheme(key) }
                                        )
                                    }
                                }
                            }

                            // Blur Intensity Selector
                            Column {
                                Text("Blur Intensity", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("low" to "Low", "medium" to "Medium", "high" to "High").forEach { (key, label) ->
                                        val isSelected = settings.blurIntensity == key
                                        SegmentPill(
                                            text = label,
                                            isSelected = isSelected,
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.settingsRepository.updateBlurIntensity(key) }
                                        )
                                    }
                                }
                            }

                            // Accent Color Palette Selector
                            Column {
                                Text("Accent Color", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    AccentColors.forEachIndexed { idx, color ->
                                        val isSelected = settings.accentColorIndex == idx
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    BorderStroke(
                                                        2.5.dp,
                                                        if (isSelected) Color.White else Color.Transparent
                                                    ),
                                                    CircleShape
                                                )
                                                .clickable { viewModel.settingsRepository.updateAccentColor(idx) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Reduce Motion Toggle
                            SettingToggleRow(
                                title = "Reduce Motion",
                                subtitle = "Minimize animations and transitions",
                                checked = settings.reduceMotion,
                                onCheckedChange = { viewModel.settingsRepository.toggleReduceMotion(it) }
                            )
                        }
                    }
                }

                // QUIZ OPTIONS SECTION matching Screen 8
                item {
                    SectionHeader("Quiz Options")
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0x30112442)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SettingToggleRow(
                                title = "Sound Effects",
                                subtitle = "Play chime audio on answers and clicks",
                                checked = settings.soundEffects,
                                onCheckedChange = { viewModel.settingsRepository.toggleSoundEffects(it) }
                            )

                            SettingToggleRow(
                                title = "Haptic Vibration",
                                subtitle = "Vibrate device when answering and navigating",
                                checked = settings.vibration,
                                onCheckedChange = { viewModel.settingsRepository.toggleVibration(it) }
                            )

                            SettingToggleRow(
                                title = "Show Explanations",
                                subtitle = "Display instant explanations in practice mode",
                                checked = settings.showExplanations,
                                onCheckedChange = { viewModel.settingsRepository.toggleShowExplanations(it) }
                            )

                            SettingToggleRow(
                                title = "Auto-Advance",
                                subtitle = "Automatically go to next question on selection",
                                checked = settings.autoAdvance,
                                onCheckedChange = { viewModel.settingsRepository.toggleAutoAdvance(it) }
                            )
                        }
                    }
                }

                // DATA & BACKUP SECTION
                item {
                    SectionHeader("Data & Backup")
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0x30112442)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Export Backup Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val backup = viewModel.repository.exportAllQuizzesBackup()
                                            withContext(Dispatchers.Main) {
                                                val sendIntent: Intent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, backup)
                                                    type = "application/json"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Export All Quizzes Backup"))
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Backup,
                                        contentDescription = null,
                                        tint = Color(0xFF54A0FF),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Export All Quizzes", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Save or share backup JSON of all quizzes", color = Color(0xB3FFFFFF), fontSize = 12.sp)
                                    }
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0x80FFFFFF), modifier = Modifier.size(16.dp))
                            }

                            // Reset to Sample Quizzes
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showResetDialog = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA502),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Restore Sample Quizzes", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Re-import General Knowledge, Science, Math...", color = Color(0xB3FFFFFF), fontSize = 12.sp)
                                    }
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0x80FFFFFF), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // ABOUT SECTION matching Screen 8
                item {
                    SectionHeader("About")
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0x280D1C34)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("App Version", color = Color(0xB3FFFFFF), fontSize = 14.sp)
                                Text("1.0.0 (Offline-First)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Architecture", color = Color(0xB3FFFFFF), fontSize = 14.sp)
                                Text("JSON-Driven Quiz Engine", color = Color(0xFF54A0FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Made with ❤️ for learners everywhere.",
                                color = Color(0x99FFFFFF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.repository.seedInitialQuizzesIfEmpty()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Sample quizzes reloaded!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showResetDialog = false
                    }
                ) {
                    Text("Restore", color = Color(0xFF2E86DE), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            title = { Text("Restore Sample Quizzes?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will add back any missing default quizzes without deleting your custom quizzes.", color = Color(0xE6FFFFFF)) },
            containerColor = Color(0xFF1E2D4A)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xD9FFFFFF),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SegmentPill(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color.White else Color(0x28FFFFFF)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF1E2D4A) else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color(0xB3FFFFFF), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2E86DE)
            )
        )
    }
}
