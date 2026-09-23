package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quiz
import com.example.parser.QuizJsonParser
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassScaffold
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SecondaryGlassButton
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun QuizDetailsScreen(
    quizId: String,
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onStartQuiz: (isPractice: Boolean) -> Unit,
    onEditQuiz: () -> Unit
) {
    val context = LocalContext.current
    val allQuizzes by viewModel.allQuizzes.collectAsState()
    val quiz = allQuizzes.firstOrNull { it.id == quizId }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (quiz == null) {
        GlassScaffold {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Quiz not found", color = Color.White, fontSize = 18.sp)
            }
        }
        return
    }

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header: Back, Title, Favorite & Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )

                Text(
                    text = "Quiz Details",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = if (quiz.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        isActive = quiz.isFavorite,
                        activeTint = Color(0xFFFF4757),
                        onClick = { viewModel.toggleFavorite(quiz) }
                    )

                    GlassIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share JSON",
                        onClick = {
                            val json = QuizJsonParser.exportToJson(quiz, quiz.questions)
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, json)
                                type = "application/json"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Quiz JSON"))
                        }
                    )
                }
            }

            // Scrollable Details Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Title and Category Banner
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0x35102545),
                    borderStroke = BorderStroke(1.2.dp, Color(0x55FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Text(
                            text = quiz.title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )

                        if (quiz.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = quiz.description,
                                color = Color(0xB3FFFFFF),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DetailPill(label = quiz.category, color = Color(0xFF00CEC9))
                            DetailPill(label = quiz.difficulty, color = Color(0xFFFFA502))
                            if (quiz.timeLimit > 0) {
                                DetailPill(label = "${quiz.timeLimit / 60} mins", color = Color(0xFF54A0FF))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Matrix (Questions, Best Score, Attempts, Time Limit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailMetricCard(
                        icon = Icons.Default.Category,
                        label = "Questions",
                        value = "${quiz.questions.size}",
                        modifier = Modifier.weight(1f)
                    )

                    DetailMetricCard(
                        icon = Icons.Default.EmojiEvents,
                        label = "Best Score",
                        value = if (quiz.attemptsCount > 0) "${quiz.bestScore}%" else "—",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailMetricCard(
                        icon = Icons.Default.School,
                        label = "Attempts",
                        value = "${quiz.attemptsCount}",
                        modifier = Modifier.weight(1f)
                    )

                    DetailMetricCard(
                        icon = Icons.Default.AccessTime,
                        label = "Time Limit",
                        value = if (quiz.timeLimit > 0) "${quiz.timeLimit}s" else "None",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Management Actions Row (Edit, Duplicate, Copy JSON, Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryGlassButton(
                        text = "Edit",
                        icon = Icons.Default.Edit,
                        onClick = onEditQuiz,
                        modifier = Modifier.weight(1f)
                    )

                    SecondaryGlassButton(
                        text = "Copy JSON",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            val json = QuizJsonParser.exportToJson(quiz, quiz.questions)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Quiz JSON", json))
                            Toast.makeText(context, "JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.2f)
                    )

                    GlassIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF6B81),
                        size = 52.dp,
                        onClick = { showDeleteDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom CTA: Start Quiz & Practice Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryGradientButton(
                    text = "Start Quiz",
                    icon = Icons.Default.PlayArrow,
                    onClick = { onStartQuiz(false) }
                )

                SecondaryGlassButton(
                    text = "Practice Mode (With Explanations)",
                    icon = Icons.Default.School,
                    onClick = { onStartQuiz(true) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQuiz(quiz.id)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF4757), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            title = {
                Text("Delete Quiz?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${quiz.title}\"? This action cannot be undone.",
                    color = Color(0xE6FFFFFF),
                    fontSize = 14.sp
                )
            },
            containerColor = Color(0xFF1E2D4A)
        )
    }
}

@Composable
private fun DetailPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DetailMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x2B102038)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x30FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = Color(0xB3FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}
