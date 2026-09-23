package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizAttemptEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassEmptyState
import com.example.ui.components.GlassScaffold
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: QuizViewModel,
    onNavigateToHome: () -> Unit
) {
    val stats by viewModel.overallStatistics.collectAsState()
    val attempts by viewModel.repository.allAttempts.collectAsState(initial = emptyList())
    var showClearConfirm by remember { mutableStateOf(false) }

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistics",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )

                if (attempts.isNotEmpty()) {
                    TextButton(onClick = { showClearConfirm = true }) {
                        Text("Clear History", color = Color(0xFFFF6B81), fontSize = 13.sp)
                    }
                }
            }

            if (attempts.isEmpty()) {
                GlassEmptyState(
                    title = "No Quiz Attempts Yet",
                    description = "Take your first quiz to track your accuracy, completion speed, and scores.",
                    buttonText = "Explore Quizzes",
                    onButtonClick = onNavigateToHome,
                    icon = Icons.Default.Leaderboard,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Accuracy & Best Score Hero Card
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = Color(0x35102545),
                            borderStroke = BorderStroke(1.2.dp, Color(0x55FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${stats.overallAccuracy}%",
                                        color = Color(0xFF2ED573),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Overall Accuracy", color = Color(0xB3FFFFFF), fontSize = 13.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(44.dp)
                                        .background(Color(0x30FFFFFF))
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${stats.highestScorePercentage}%",
                                        color = Color(0xFF00CEC9),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Highest Score", color = Color(0xB3FFFFFF), fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // 4 Metric Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatTile(
                                title = "Total Quizzes",
                                value = "${stats.totalQuizzes}",
                                icon = Icons.Default.Folder,
                                color = Color(0xFF10AC84),
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                title = "Attempts",
                                value = "${stats.totalAttempts}",
                                icon = Icons.Default.Timeline,
                                color = Color(0xFF2E86DE),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatTile(
                                title = "Questions Done",
                                value = "${stats.totalQuestionsAnswered}",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF5F27CD),
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                title = "Avg Time",
                                value = "${stats.averageCompletionTimeSeconds}s",
                                icon = Icons.Default.AccessTime,
                                color = Color(0xFFFFA502),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Recent Attempts Section Header
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Recent Attempts",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Attempts List
                    items(attempts, key = { it.id }) { attempt ->
                        AttemptHistoryCard(attempt = attempt)
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.repository.clearHistory()
                        }
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear", color = Color(0xFFFF6B81), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            title = { Text("Clear Statistics History?", color = Color.White) },
            text = { Text("This will erase all past attempt records and reset your metrics.", color = Color(0xE6FFFFFF)) },
            containerColor = Color(0xFF1E2D4A)
        )
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x30112442)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.25f))
                    .border(BorderStroke(1.dp, color.copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = title, color = Color(0xB3FFFFFF), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AttemptHistoryCard(attempt: QuizAttemptEntity) {
    val dateStr = remember(attempt.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
        sdf.format(Date(attempt.timestamp))
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color(0x28122645)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.quizTitle,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr · ${attempt.timeTakenSeconds}s",
                    color = Color(0x99FFFFFF),
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (attempt.percentage >= 70) Color(0x3510AC84) else Color(0x35FF4757)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (attempt.percentage >= 70) Color(0xFF2ED573) else Color(0xFFFF6B81)
                        ),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${attempt.percentage}%",
                    color = if (attempt.percentage >= 70) Color(0xFF2ED573) else Color(0xFFFF6B81),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
