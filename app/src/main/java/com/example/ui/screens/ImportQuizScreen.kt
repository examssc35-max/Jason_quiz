package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quiz
import com.example.parser.QuizJsonParser
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassScaffold
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SecondaryGlassButton
import com.example.ui.viewmodel.ImportUiState
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun ImportQuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onQuizImported: (quizId: String) -> Unit
) {
    val context = LocalContext.current
    val importStatus by viewModel.importStatus.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSampleDialog by remember { mutableStateOf(false) }

    // Native Android SAF File Picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importJsonFromUri(context, uri)
        }
    }

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // Header matching Screen 3: Back button, "Import Quiz", Help "?"
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
                    onClick = {
                        viewModel.resetImportState()
                        onNavigateBack()
                    }
                )

                Text(
                    text = "Import Quiz",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassIconButton(
                    icon = Icons.Default.HelpOutline,
                    contentDescription = "Help",
                    onClick = { showHelpDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Orbital Glass Ring with JSON Document Badge matching Screen 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Orbital rings
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .border(BorderStroke(1.2.dp, Color(0x359BB5E8)), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(135.dp)
                        .border(BorderStroke(1.5.dp, Color(0x55A55EEA)), CircleShape)
                )

                // Frosted Glowing Center Document
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF8854D0))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0x60D1D8E0), Color(0x30A55EEA))
                            )
                        )
                        .border(BorderStroke(1.5.dp, Color(0x80FFFFFF)), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "JSON",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Subtitle matching Screen 3
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select a JSON File",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Import your quiz and start learning instantly. Supports custom quiz format.",
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Primary Pill Button: "📁 Choose File"
                PrimaryGradientButton(
                    text = "Choose File",
                    icon = Icons.Default.FolderOpen,
                    isLoading = importStatus is ImportUiState.Loading,
                    onClick = {
                        viewModel.resetImportState()
                        filePickerLauncher.launch(
                            arrayOf("application/json", "text/plain", "*/*")
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Validation Results / Feedback Banner
            AnimatedVisibility(visible = importStatus is ImportUiState.Success) {
                val successQuiz = (importStatus as? ImportUiState.Success)?.quiz
                if (successQuiz != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        backgroundColor = Color(0x4010AC84),
                        borderStroke = BorderStroke(1.2.dp, Color(0xFF2ED573))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2ED573),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Quiz Imported Successfully!",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${successQuiz.title}\" with ${successQuiz.questions.size} questions is ready.",
                                color = Color(0xE6FFFFFF),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            PrimaryGradientButton(
                                text = "Start Quiz Now",
                                icon = Icons.Default.PlayArrow,
                                onClick = { onQuizImported(successQuiz.id) }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = importStatus is ImportUiState.Error) {
                val errors = (importStatus as? ImportUiState.Error)?.errors ?: emptyList()
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    backgroundColor = Color(0x40FF4757),
                    borderStroke = BorderStroke(1.2.dp, Color(0xFFFF6B81))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF6B81),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Validation Failed",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        errors.forEach { err ->
                            Text(
                                text = "• $err",
                                color = Color(0xF2FFFFFF),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Example JSON Format Preview Card matching Screen 3
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = Color(0x28122645),
                borderStroke = BorderStroke(1.dp, Color(0x45FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Example JSON Format",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x35000000))
                            .border(BorderStroke(1.dp, Color(0x25FFFFFF)), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = """{
  "title": "General Knowledge",
  "category": "General Knowledge",
  "difficulty": "Easy",
  "timeLimit": 300,
  "questions": [ ... ]
}""",
                            color = Color(0xD9FFFFFF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row button: "📄 View Sample File >"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSampleDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF54A0FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Sample File",
                                color = Color(0xFF54A0FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF54A0FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got It", color = Color(0xFF2E86DE), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "JSON Quiz Schema",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Your JSON quiz file must contain:\n\n" +
                                "• title (string): The quiz title\n" +
                                "• category (string, optional): General Knowledge, Science, etc.\n" +
                                "• difficulty (string, optional): Easy, Medium, Hard\n" +
                                "• timeLimit (integer seconds, optional): e.g. 600\n" +
                                "• questions (array of objects):\n" +
                                "    - id (string): Unique question ID\n" +
                                "    - question (string): Question text\n" +
                                "    - options (array of strings): Minimum 2 options\n" +
                                "    - answer (integer): 0-based index of correct option\n" +
                                "    - points (integer, optional): Points for question\n" +
                                "    - explanation (string, optional): Review explanation",
                        color = Color(0xE6FFFFFF),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            containerColor = Color(0xFF1E2D4A)
        )
    }

    // Sample File Preview Dialog
    if (showSampleDialog) {
        AlertDialog(
            onDismissRequest = { showSampleDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.validateAndSaveJson(QuizJsonParser.sampleGeneralKnowledgeJson)
                        showSampleDialog = false
                    }
                ) {
                    Text("Load This Sample", color = Color(0xFF2E86DE), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSampleDialog = false }) {
                    Text("Close", color = Color(0xB3FFFFFF))
                }
            },
            title = {
                Text("Sample Quiz JSON", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0x40000000), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = QuizJsonParser.sampleGeneralKnowledgeJson,
                        color = Color(0xF0FFFFFF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            containerColor = Color(0xFF1E2D4A)
        )
    }
}
