package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassOptionButton
import com.example.ui.components.GlassScaffold
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.theme.LocalGlassColors
import com.example.ui.viewmodel.GameplayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayScreen(
    viewModel: GameplayViewModel,
    onNavigateBack: () -> Unit,
    onQuizFinished: () -> Unit
) {
    val quiz by viewModel.quiz.collectAsState()
    val questions by viewModel.orderedQuestions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val remainingTime by viewModel.remainingTimeSeconds.collectAsState()
    val isTimeUp by viewModel.isTimeUp.collectAsState()
    val quizResult by viewModel.quizResult.collectAsState()
    val settings by viewModel.settingsRepository.settings.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showNavigatorSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(quizResult) {
        if (quizResult != null) {
            onQuizFinished()
        }
    }

    if (questions.isEmpty()) {
        GlassScaffold {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading quiz...", color = Color.White, fontSize = 18.sp)
            }
        }
        return
    }

    val currentQuestion = questions.getOrElse(currentIndex) { questions.first() }
    val selectedOption = selectedAnswers[currentQuestion.id]
    val isBookmarked = bookmarkedIds.contains(currentQuestion.id)
    val totalCount = questions.size

    val progressAnim by animateFloatAsState(
        targetValue = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f,
        label = "progress_anim"
    )

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header matching Screen 4: Close "✕", "Question 3 of 10", Timer "⏱ 00:27"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Exit Quiz",
                    onClick = { showExitDialog = true }
                )

                Text(
                    text = "Question ${currentIndex + 1} of $totalCount",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Timer Pill (if timed quiz)
                if (quiz != null && quiz!!.timeLimit > 0) {
                    val minutes = remainingTime / 60
                    val seconds = remainingTime % 60
                    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                    val isLowTime = remainingTime <= 30

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isLowTime) Color(0x66FF4757) else Color(0x35FFFFFF)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isLowTime) Color(0xFFFF6B81) else Color(0x55FFFFFF)
                                ),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Timer",
                                tint = if (isLowTime) Color(0xFFFF6B81) else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timeFormatted,
                                color = if (isLowTime) Color(0xFFFF6B81) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Spacer placeholder to balance layout
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }

            // Sleek Progress Bar just below Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x30FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnim)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00CEC9), Color(0xFF2E86DE))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Question & Options Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Main Question Glass Card matching Screen 4
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0x35112543),
                    borderStroke = BorderStroke(1.2.dp, Color(0x55FFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Bookmark icon top right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            GlassIconButton(
                                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                isActive = isBookmarked,
                                activeTint = Color(0xFFFFA502),
                                size = 36.dp,
                                iconSize = 20.dp,
                                onClick = { viewModel.toggleBookmark(currentQuestion.id) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Question Text
                        Text(
                            text = currentQuestion.question,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )

                        if (currentQuestion.points > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x28FFFFFF))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${currentQuestion.points} Points",
                                    color = Color(0xFF54A0FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Answer Options matching Screen 4
                val optionLetters = listOf("A", "B", "C", "D", "E", "F", "G")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    currentQuestion.options.forEachIndexed { idx, optText ->
                        val letter = optionLetters.getOrElse(idx) { "${idx + 1}" }
                        val isSelected = selectedOption == idx

                        val isCorrectStatus: Boolean? = if (viewModel.isPracticeMode && selectedOption != null) {
                            when {
                                idx == currentQuestion.answer -> true
                                isSelected -> false
                                else -> null
                            }
                        } else null

                        GlassOptionButton(
                            letter = letter,
                            text = optText,
                            isSelected = isSelected,
                            isCorrect = isCorrectStatus,
                            onClick = {
                                viewModel.selectOption(currentQuestion.id, idx)
                            }
                        )
                    }
                }

                // Practice Mode Instant Explanation Box
                if (viewModel.isPracticeMode && selectedOption != null && !currentQuestion.explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0x3010AC84),
                        borderStroke = BorderStroke(1.dp, Color(0x6010AC84)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Explanation",
                                tint = Color(0xFF2ED573),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = currentQuestion.explanation!!,
                                color = Color(0xF2FFFFFF),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // Bottom Action Bar matching Screen 4
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button (circular frosted glass)
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Question",
                    size = 56.dp,
                    iconSize = 24.dp,
                    onClick = { viewModel.prevQuestion() }
                )

                // Question Navigator Button (circular frosted glass with Grid icon)
                GlassIconButton(
                    icon = Icons.Default.GridView,
                    contentDescription = "Question Navigator",
                    size = 56.dp,
                    iconSize = 24.dp,
                    onClick = { showNavigatorSheet = true }
                )

                // Next or Submit Button (pill gradient button)
                val isLastQuestion = currentIndex == totalCount - 1
                PrimaryGradientButton(
                    text = if (isLastQuestion) "Submit Quiz" else "Next →",
                    onClick = {
                        if (isLastQuestion) {
                            viewModel.submitQuiz()
                        } else {
                            viewModel.nextQuestion()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Question Navigator Bottom Sheet
    if (showNavigatorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNavigatorSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF13233C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Question Navigator",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Legend row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem("Answered", Color(0xFF2E86DE))
                    LegendItem("Current", Color(0xFF00CEC9))
                    LegendItem("Unanswered", Color(0x35FFFFFF))
                }

                Spacer(modifier = Modifier.height(18.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    itemsIndexed(questions) { idx, q ->
                        val isCurrent = idx == currentIndex
                        val isAnswered = selectedAnswers.containsKey(q.id)

                        val bg = when {
                            isCurrent -> Color(0xFF00CEC9)
                            isAnswered -> Color(0xFF2E86DE)
                            else -> Color(0x28FFFFFF)
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isCurrent) Color.White else Color(0x55FFFFFF)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.jumpToQuestion(idx)
                                    showNavigatorSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveProgressBeforeExit()
                        showExitDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Save & Exit", color = Color(0xFF2E86DE), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = Color(0xFFFF6B81))
                }
            },
            title = {
                Text("Exit Quiz?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You can save your current answers and resume later from the Home screen.",
                    color = Color(0xE6FFFFFF),
                    fontSize = 14.sp
                )
            },
            containerColor = Color(0xFF1E2D4A)
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color(0xB3FFFFFF), fontSize = 12.sp)
    }
}
