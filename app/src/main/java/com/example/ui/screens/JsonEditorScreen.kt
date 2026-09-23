package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Question
import com.example.model.Quiz
import com.example.parser.QuizJsonParser
import com.example.parser.ValidationResult
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassScaffold
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.SecondaryGlassButton
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun JsonEditorScreen(
    quizId: String?,
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onQuizSaved: (savedQuizId: String) -> Unit
) {
    val context = LocalContext.current
    val allQuizzes by viewModel.allQuizzes.collectAsState()
    val existingQuiz = allQuizzes.firstOrNull { it.id == quizId }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Visual Editor, 1 = Raw JSON

    // Form state for Visual Editor
    var title by remember { mutableStateOf(existingQuiz?.title ?: "New Quiz") }
    var description by remember { mutableStateOf(existingQuiz?.description ?: "") }
    var category by remember { mutableStateOf(existingQuiz?.category ?: "General Knowledge") }
    var difficulty by remember { mutableStateOf(existingQuiz?.difficulty ?: "Medium") }
    var timeLimitStr by remember { mutableStateOf(existingQuiz?.timeLimit?.toString() ?: "300") }
    var shuffleQuestions by remember { mutableStateOf(existingQuiz?.shuffleQuestions ?: true) }
    var shuffleOptions by remember { mutableStateOf(existingQuiz?.shuffleOptions ?: true) }

    val questionsState = remember {
        mutableStateListOf<Question>().apply {
            if (existingQuiz != null && existingQuiz.questions.isNotEmpty()) {
                addAll(existingQuiz.questions)
            } else {
                add(
                    Question(
                        id = "q1",
                        question = "What is the capital of France?",
                        options = listOf("Berlin", "Madrid", "Paris", "Rome"),
                        answer = 2,
                        points = 1,
                        explanation = "Paris is the capital of France."
                    )
                )
            }
        }
    }

    // Raw JSON state
    var rawJsonText by remember {
        val initialJson = if (existingQuiz != null) {
            QuizJsonParser.exportToJson(existingQuiz, existingQuiz.questions)
        } else {
            QuizJsonParser.exportToJson(
                Quiz(
                    id = UUID.randomUUID().toString(),
                    title = "New Quiz",
                    category = "General Knowledge",
                    difficulty = "Medium",
                    timeLimit = 300,
                    questions = questionsState.toList()
                )
            )
        }
        mutableStateOf(initialJson)
    }

    GlassScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header: Back, Title, Save Button
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
                    text = if (existingQuiz != null) "Edit Quiz" else "Create Quiz",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassIconButton(
                    icon = Icons.Default.Save,
                    contentDescription = "Save",
                    activeTint = Color(0xFF2ED573),
                    onClick = {
                        if (selectedTab == 0) {
                            // Save from Visual Editor
                            val tLimit = timeLimitStr.toIntOrNull() ?: 0
                            val targetId = existingQuiz?.id ?: UUID.randomUUID().toString()
                            val updatedQuiz = Quiz(
                                id = targetId,
                                title = title.trim(),
                                description = description.trim(),
                                category = category.trim(),
                                difficulty = difficulty.trim(),
                                timeLimit = tLimit,
                                shuffleQuestions = shuffleQuestions,
                                shuffleOptions = shuffleOptions,
                                questions = questionsState.toList(),
                                createdAt = existingQuiz?.createdAt ?: System.currentTimeMillis()
                            )
                            val json = QuizJsonParser.exportToJson(updatedQuiz, updatedQuiz.questions)
                            when (val res = QuizJsonParser.parseAndValidate(json, existingId = targetId)) {
                                is ValidationResult.Success -> {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        viewModel.repository.saveQuiz(res.quiz)
                                    }
                                    Toast.makeText(context, "Quiz saved successfully!", Toast.LENGTH_SHORT).show()
                                    onQuizSaved(targetId)
                                }
                                is ValidationResult.Error -> {
                                    Toast.makeText(context, res.errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            // Save from Raw JSON
                            when (val res = QuizJsonParser.parseAndValidate(rawJsonText, existingId = existingQuiz?.id)) {
                                is ValidationResult.Success -> {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        viewModel.repository.saveQuiz(res.quiz)
                                    }
                                    Toast.makeText(context, "Quiz saved successfully!", Toast.LENGTH_SHORT).show()
                                    onQuizSaved(res.quiz.id)
                                }
                                is ValidationResult.Error -> {
                                    Toast.makeText(context, res.errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            }

            // Tab Row: [ Visual Editor | Raw JSON ]
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF54A0FF),
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        if (selectedTab == 1) {
                            // Try parsing raw json to update visual editor
                            when (val parsed = QuizJsonParser.parseAndValidate(rawJsonText)) {
                                is ValidationResult.Success -> {
                                    title = parsed.quiz.title
                                    description = parsed.quiz.description
                                    category = parsed.quiz.category
                                    difficulty = parsed.quiz.difficulty
                                    timeLimitStr = parsed.quiz.timeLimit.toString()
                                    shuffleQuestions = parsed.quiz.shuffleQuestions
                                    shuffleOptions = parsed.quiz.shuffleOptions
                                    questionsState.clear()
                                    questionsState.addAll(parsed.quiz.questions)
                                }
                                is ValidationResult.Error -> {}
                            }
                        }
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            text = "Visual Editor",
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        if (selectedTab == 0) {
                            // Update raw json from visual editor
                            val tLimit = timeLimitStr.toIntOrNull() ?: 0
                            val q = Quiz(
                                id = existingQuiz?.id ?: UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                category = category,
                                difficulty = difficulty,
                                timeLimit = tLimit,
                                shuffleQuestions = shuffleQuestions,
                                shuffleOptions = shuffleOptions,
                                questions = questionsState.toList()
                            )
                            rawJsonText = QuizJsonParser.exportToJson(q, q.questions)
                        }
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            text = "Raw JSON",
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            if (selectedTab == 0) {
                // VISUAL EDITOR
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Metadata Card
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            backgroundColor = Color(0x35102545)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Quiz Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                EditorTextField(label = "Title", value = title, onValueChange = { title = it })
                                Spacer(modifier = Modifier.height(10.dp))

                                EditorTextField(label = "Description", value = description, onValueChange = { description = it })
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    EditorTextField(label = "Category", value = category, onValueChange = { category = it }, modifier = Modifier.weight(1f))
                                    EditorTextField(label = "Difficulty", value = difficulty, onValueChange = { difficulty = it }, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                EditorTextField(label = "Time Limit (seconds, 0 for none)", value = timeLimitStr, onValueChange = { timeLimitStr = it })
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Shuffle Questions", color = Color.White, fontSize = 14.sp)
                                    Switch(
                                        checked = shuffleQuestions,
                                        onCheckedChange = { shuffleQuestions = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2E86DE))
                                    )
                                }
                            }
                        }
                    }

                    // Questions Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Questions (${questionsState.size})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            SecondaryGlassButton(
                                text = "+ Add Question",
                                icon = Icons.Default.Add,
                                onClick = {
                                    questionsState.add(
                                        Question(
                                            id = "q_${questionsState.size + 1}",
                                            question = "New question text...",
                                            options = listOf("Option 1", "Option 2", "Option 3", "Option 4"),
                                            answer = 0,
                                            points = 1,
                                            explanation = ""
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Questions List
                    itemsIndexed(questionsState) { qIdx, question ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0x30112442)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Question #${qIdx + 1}", color = Color(0xFF54A0FF), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                                    if (questionsState.size > 1) {
                                        GlassIconButton(
                                            icon = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFFF6B81),
                                            size = 36.dp,
                                            iconSize = 18.dp,
                                            onClick = { questionsState.removeAt(qIdx) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                EditorTextField(
                                    label = "Question text",
                                    value = question.question,
                                    onValueChange = { newQ ->
                                        questionsState[qIdx] = question.copy(question = newQ)
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Options (Select correct radio):", color = Color(0xB3FFFFFF), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                question.options.forEachIndexed { optIdx, optText ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        RadioButton(
                                            selected = question.answer == optIdx,
                                            onClick = {
                                                questionsState[qIdx] = question.copy(answer = optIdx)
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF2ED573),
                                                unselectedColor = Color(0x80FFFFFF)
                                            )
                                        )
                                        EditorTextField(
                                            label = "Option ${optIdx + 1}",
                                            value = optText,
                                            onValueChange = { newOpt ->
                                                val opts = question.options.toMutableList()
                                                opts[optIdx] = newOpt
                                                questionsState[qIdx] = question.copy(options = opts)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                EditorTextField(
                                    label = "Explanation (Optional)",
                                    value = question.explanation ?: "",
                                    onValueChange = { newExp ->
                                        questionsState[qIdx] = question.copy(explanation = newExp.ifBlank { null })
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // RAW JSON EDITOR
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecondaryGlassButton(
                            text = "Format / Pretty Print",
                            icon = Icons.Default.AutoFixHigh,
                            onClick = {
                                when (val res = QuizJsonParser.parseAndValidate(rawJsonText)) {
                                    is ValidationResult.Success -> {
                                        rawJsonText = QuizJsonParser.exportToJson(res.quiz, res.quiz.questions)
                                        Toast.makeText(context, "Formatted successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    is ValidationResult.Error -> {
                                        Toast.makeText(context, "Cannot format invalid JSON: ${res.errors.firstOrNull()}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryGlassButton(
                            text = "Validate",
                            icon = Icons.Default.Check,
                            onClick = {
                                when (val res = QuizJsonParser.parseAndValidate(rawJsonText)) {
                                    is ValidationResult.Success -> {
                                        Toast.makeText(context, "Valid JSON! Quiz has ${res.quiz.questions.size} questions.", Toast.LENGTH_SHORT).show()
                                    }
                                    is ValidationResult.Error -> {
                                        Toast.makeText(context, res.errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x40000000))
                            .border(BorderStroke(1.dp, Color(0x35FFFFFF)), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        BasicTextField(
                            value = rawJsonText,
                            onValueChange = { rawJsonText = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF54A0FF)),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EditorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = Color(0xB3FFFFFF), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x28FFFFFF))
                .border(BorderStroke(1.dp, Color(0x35FFFFFF)), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
