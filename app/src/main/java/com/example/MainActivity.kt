package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.QuizResult
import com.example.ui.components.AppDestination
import com.example.ui.components.GlassBottomNavigation
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImportQuizScreen
import com.example.ui.screens.JsonEditorScreen
import com.example.ui.screens.MyQuizzesScreen
import com.example.ui.screens.QuizDetailsScreen
import com.example.ui.screens.QuizPlayScreen
import com.example.ui.screens.QuizResultScreen
import com.example.ui.screens.ReviewAnswersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameplayViewModel
import com.example.ui.viewmodel.QuizViewModel

sealed class Screen {
    data class Main(val tab: AppDestination = AppDestination.HOME) : Screen()
    data object Import : Screen()
    data class Details(val quizId: String) : Screen()
    data class Play(val quizId: String, val isPractice: Boolean = false) : Screen()
    data class Result(val result: QuizResult) : Screen()
    data class Review(val result: QuizResult) : Screen()
    data class Editor(val quizId: String?) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val quizViewModel: QuizViewModel = viewModel()
            val settings by quizViewModel.settings.collectAsState()

            MyApplicationTheme(
                themePreference = settings.theme,
                accentIndex = settings.accentColorIndex
            ) {
                QuizExploreApp(quizViewModel = quizViewModel)
            }
        }
    }
}

@Composable
fun QuizExploreApp(quizViewModel: QuizViewModel) {
    var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Main(AppDestination.HOME))) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Main(AppDestination.HOME)

    fun navigateTo(screen: Screen) {
        screenStack = screenStack + screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        }
    }

    fun navigateToMain(tab: AppDestination = AppDestination.HOME) {
        screenStack = listOf(Screen.Main(tab))
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Main -> {
                var currentTab by remember { mutableStateOf(screen.tab) }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                viewModel = quizViewModel,
                                onNavigateToImport = { navigateTo(Screen.Import) },
                                onNavigateToQuizzes = { filter ->
                                    if (filter != null) {
                                        quizViewModel.setSelectedFilter(filter)
                                    }
                                    currentTab = AppDestination.QUIZZES
                                },
                                onNavigateToStats = { currentTab = AppDestination.STATS },
                                onStartQuiz = { quizId -> navigateTo(Screen.Play(quizId = quizId)) },
                                onOpenQuizDetails = { quizId -> navigateTo(Screen.Details(quizId = quizId)) }
                            )
                        }
                        AppDestination.QUIZZES -> {
                            MyQuizzesScreen(
                                viewModel = quizViewModel,
                                onStartQuiz = { quizId -> navigateTo(Screen.Play(quizId = quizId)) },
                                onOpenQuizDetails = { quizId -> navigateTo(Screen.Details(quizId = quizId)) },
                                onNavigateToImport = { navigateTo(Screen.Import) },
                                onNavigateToCreate = { navigateTo(Screen.Editor(null)) }
                            )
                        }
                        AppDestination.STATS -> {
                            StatisticsScreen(
                                viewModel = quizViewModel,
                                onNavigateToHome = { currentTab = AppDestination.HOME }
                            )
                        }
                        AppDestination.SETTINGS -> {
                            SettingsScreen(viewModel = quizViewModel)
                        }
                    }

                    // Floating Glass Bottom Navigation
                    GlassBottomNavigation(
                        currentDestination = currentTab,
                        onDestinationSelected = { currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            is Screen.Import -> {
                ImportQuizScreen(
                    viewModel = quizViewModel,
                    onNavigateBack = { navigateBack() },
                    onQuizImported = { quizId ->
                        // Replaces import with Details or Play
                        screenStack = listOf(Screen.Main(AppDestination.QUIZZES), Screen.Details(quizId))
                    }
                )
            }

            is Screen.Details -> {
                QuizDetailsScreen(
                    quizId = screen.quizId,
                    viewModel = quizViewModel,
                    onNavigateBack = { navigateBack() },
                    onStartQuiz = { isPractice ->
                        navigateTo(Screen.Play(quizId = screen.quizId, isPractice = isPractice))
                    },
                    onEditQuiz = {
                        navigateTo(Screen.Editor(quizId = screen.quizId))
                    }
                )
            }

            is Screen.Play -> {
                val gameplayViewModel = remember(screen.quizId, screen.isPractice) {
                    GameplayViewModel(
                        application = quizViewModel.getApplication(),
                        quizId = screen.quizId,
                        isPracticeMode = screen.isPractice
                    )
                }

                QuizPlayScreen(
                    viewModel = gameplayViewModel,
                    onNavigateBack = { navigateBack() },
                    onQuizFinished = {
                        val res = gameplayViewModel.quizResult.value
                        if (res != null) {
                            screenStack = listOf(Screen.Main(AppDestination.HOME), Screen.Result(res))
                        } else {
                            navigateBack()
                        }
                    }
                )
            }

            is Screen.Result -> {
                QuizResultScreen(
                    result = screen.result,
                    onReviewAnswers = { navigateTo(Screen.Review(screen.result)) },
                    onTryAgain = {
                        screenStack = listOf(Screen.Main(AppDestination.HOME), Screen.Play(quizId = screen.result.quizId))
                    },
                    onBackHome = { navigateToMain(AppDestination.HOME) }
                )
            }

            is Screen.Review -> {
                ReviewAnswersScreen(
                    result = screen.result,
                    onNavigateBack = { navigateBack() }
                )
            }

            is Screen.Editor -> {
                JsonEditorScreen(
                    quizId = screen.quizId,
                    viewModel = quizViewModel,
                    onNavigateBack = { navigateBack() },
                    onQuizSaved = { savedId ->
                        screenStack = listOf(Screen.Main(AppDestination.QUIZZES), Screen.Details(savedId))
                    }
                )
            }
        }
    }
}
