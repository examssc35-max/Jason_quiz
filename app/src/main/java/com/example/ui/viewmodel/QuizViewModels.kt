package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QuizDatabase
import com.example.data.QuizProgressEntity
import com.example.data.QuizRepository
import com.example.data.SettingsRepository
import com.example.data.UserSettings
import com.example.model.OverallStatistics
import com.example.model.Question
import com.example.model.Quiz
import com.example.model.QuizResult
import com.example.model.SavedQuizProgress
import com.example.model.UserAnswer
import com.example.parser.QuizJsonParser
import com.example.parser.ValidationResult
import com.example.util.SoundAndHapticManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val database = QuizDatabase.getInstance(application)
    val repository = QuizRepository(
        database.quizDao(),
        database.quizAttemptDao(),
        database.quizProgressDao()
    )
    val settingsRepository = SettingsRepository(application)
    val soundManager = SoundAndHapticManager(application)

    val settings: StateFlow<UserSettings> = settingsRepository.settings

    val allQuizzes: StateFlow<List<Quiz>> = repository.allQuizzes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteQuizzes: StateFlow<List<Quiz>> = repository.favoriteQuizzes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentQuizzes: StateFlow<List<Quiz>> = repository.recentQuizzes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeProgressList: StateFlow<List<QuizProgressEntity>> = repository.activeProgressList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val overallStatistics: StateFlow<OverallStatistics> = repository.overallStatistics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverallStatistics()
    )

    // Search and filter state for library
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All") // "All", "Favorites", "Recent", "Category:..."
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val filteredQuizzes: StateFlow<List<Quiz>> = combine(
        allQuizzes,
        _searchQuery,
        _selectedFilter
    ) { quizzes, query, filter ->
        quizzes.filter { quiz ->
            val matchesQuery = query.isBlank() ||
                    quiz.title.contains(query, ignoreCase = true) ||
                    quiz.category.contains(query, ignoreCase = true) ||
                    quiz.description.contains(query, ignoreCase = true)

            val matchesFilter = when {
                filter == "All" -> true
                filter == "Favorites" -> quiz.isFavorite
                filter == "Recent" -> quiz.lastPlayedAt > 0
                filter.startsWith("Cat:") -> quiz.category.equals(filter.removePrefix("Cat:"), ignoreCase = true)
                filter.startsWith("Diff:") -> quiz.difficulty.equals(filter.removePrefix("Diff:"), ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Import state
    private val _importStatus = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val importStatus: StateFlow<ImportUiState> = _importStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialQuizzesIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleFavorite(quiz: Quiz) {
        viewModelScope.launch {
            val newFav = !quiz.isFavorite
            repository.toggleFavorite(quiz.id, newFav)
            soundManager.playClickSound(settings.value.soundEffects)
            soundManager.triggerLightHaptic(settings.value.vibration)
        }
    }

    fun duplicateQuiz(quizId: String, onComplete: (Quiz?) -> Unit) {
        viewModelScope.launch {
            val copy = repository.duplicateQuiz(quizId)
            soundManager.playClickSound(settings.value.soundEffects)
            onComplete(copy)
        }
    }

    fun deleteQuiz(quizId: String) {
        viewModelScope.launch {
            repository.deleteQuiz(quizId)
            soundManager.triggerLightHaptic(settings.value.vibration)
        }
    }

    fun importJsonFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importStatus.value = ImportUiState.Loading
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Could not read file from storage.")
                    BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                }

                validateAndSaveJson(jsonString)
            } catch (e: Exception) {
                _importStatus.value = ImportUiState.Error(listOf(e.localizedMessage ?: "Failed to open or read file."))
                soundManager.triggerErrorHaptic(settings.value.vibration)
            }
        }
    }

    fun validateAndSaveJson(jsonString: String) {
        viewModelScope.launch {
            _importStatus.value = ImportUiState.Loading
            when (val result = QuizJsonParser.parseAndValidate(jsonString)) {
                is ValidationResult.Success -> {
                    repository.saveQuiz(result.quiz)
                    _importStatus.value = ImportUiState.Success(result.quiz)
                    soundManager.playCorrectSound(settings.value.soundEffects)
                    soundManager.triggerSuccessHaptic(settings.value.vibration)
                }
                is ValidationResult.Error -> {
                    _importStatus.value = ImportUiState.Error(result.errors)
                    soundManager.playWrongSound(settings.value.soundEffects)
                    soundManager.triggerErrorHaptic(settings.value.vibration)
                }
            }
        }
    }

    fun resetImportState() {
        _importStatus.value = ImportUiState.Idle
    }
}

sealed class ImportUiState {
    data object Idle : ImportUiState()
    data object Loading : ImportUiState()
    data class Success(val quiz: Quiz) : ImportUiState()
    data class Error(val errors: List<String>) : ImportUiState()
}

/**
 * ViewModel managing the active Quiz Gameplay session, timer, and results.
 */
class GameplayViewModel(
    application: Application,
    private val quizId: String,
    val isPracticeMode: Boolean = false
) : AndroidViewModel(application) {

    private val database = QuizDatabase.getInstance(application)
    private val repository = QuizRepository(
        database.quizDao(),
        database.quizAttemptDao(),
        database.quizProgressDao()
    )
    val settingsRepository = SettingsRepository(application)
    val soundManager = SoundAndHapticManager(application)

    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _orderedQuestions = MutableStateFlow<List<Question>>(emptyList())
    val orderedQuestions: StateFlow<List<Question>> = _orderedQuestions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, Int>> = _selectedAnswers.asStateFlow()

    private val _bookmarkedIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedIds: StateFlow<Set<String>> = _bookmarkedIds.asStateFlow()

    private val _remainingTimeSeconds = MutableStateFlow(0)
    val remainingTimeSeconds: StateFlow<Int> = _remainingTimeSeconds.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isTimeUp = MutableStateFlow(false)
    val isTimeUp: StateFlow<Boolean> = _isTimeUp.asStateFlow()

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadQuizAndSession()
    }

    private fun loadQuizAndSession() {
        viewModelScope.launch {
            val q = repository.getQuizById(quizId) ?: return@launch
            _quiz.value = q

            // Check if there is saved progress
            val saved = repository.getProgress(quizId)
            val questions = if (saved != null && saved.questionOrder.isNotEmpty()) {
                val map = q.questions.associateBy { it.id }
                saved.questionOrder.mapNotNull { map[it] }
            } else {
                if (q.shuffleQuestions) q.questions.shuffled() else q.questions
            }

            _orderedQuestions.value = questions

            if (saved != null) {
                _currentIndex.value = saved.currentQuestionIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
                _selectedAnswers.value = saved.selectedAnswers
                _bookmarkedIds.value = saved.bookmarkedQuestionIds
                _remainingTimeSeconds.value = saved.remainingTimeSeconds
                _elapsedSeconds.value = saved.elapsedSeconds
            } else {
                _currentIndex.value = 0
                _selectedAnswers.value = emptyMap()
                _bookmarkedIds.value = emptySet()
                _remainingTimeSeconds.value = q.timeLimit
                _elapsedSeconds.value = 0
            }

            startTimerIfNeeded(q.timeLimit)
        }
    }

    private fun startTimerIfNeeded(timeLimit: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _elapsedSeconds.value += 1
                if (timeLimit > 0) {
                    val rem = _remainingTimeSeconds.value - 1
                    _remainingTimeSeconds.value = rem
                    if (rem <= 0) {
                        _isTimeUp.value = true
                        submitQuiz()
                        break
                    }
                }
            }
        }
    }

    fun selectOption(questionId: String, optionIndex: Int) {
        val currentAnswers = _selectedAnswers.value.toMutableMap()
        currentAnswers[questionId] = optionIndex
        _selectedAnswers.value = currentAnswers

        val settings = settingsRepository.settings.value
        soundManager.playClickSound(settings.soundEffects)
        soundManager.triggerLightHaptic(settings.vibration)

        if (settings.autoAdvance && _currentIndex.value < _orderedQuestions.value.size - 1) {
            viewModelScope.launch {
                delay(250L)
                nextQuestion()
            }
        }
    }

    fun toggleBookmark(questionId: String) {
        val set = _bookmarkedIds.value.toMutableSet()
        if (set.contains(questionId)) set.remove(questionId) else set.add(questionId)
        _bookmarkedIds.value = set
        soundManager.triggerLightHaptic(settingsRepository.settings.value.vibration)
    }

    fun nextQuestion() {
        if (_currentIndex.value < _orderedQuestions.value.size - 1) {
            _currentIndex.value += 1
            soundManager.playClickSound(settingsRepository.settings.value.soundEffects)
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            soundManager.playClickSound(settingsRepository.settings.value.soundEffects)
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in _orderedQuestions.value.indices) {
            _currentIndex.value = index
            soundManager.playClickSound(settingsRepository.settings.value.soundEffects)
        }
    }

    fun saveProgressBeforeExit() {
        val q = _quiz.value ?: return
        if (_quizResult.value != null) return // Already completed

        viewModelScope.launch {
            val progress = SavedQuizProgress(
                quizId = q.id,
                currentQuestionIndex = _currentIndex.value,
                selectedAnswers = _selectedAnswers.value,
                bookmarkedQuestionIds = _bookmarkedIds.value,
                questionOrder = _orderedQuestions.value.map { it.id },
                remainingTimeSeconds = _remainingTimeSeconds.value,
                elapsedSeconds = _elapsedSeconds.value
            )
            repository.saveProgress(progress, q.title)
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val q = _quiz.value ?: return
        val questions = _orderedQuestions.value
        val answers = _selectedAnswers.value

        var correctCount = 0
        var wrongCount = 0
        var skippedCount = 0
        var earnedPoints = 0
        var totalPoints = 0

        val userAnswersList = mutableListOf<UserAnswer>()

        for (quest in questions) {
            val selected = answers[quest.id] ?: -1
            val isCorrect = selected != -1 && selected == quest.answer
            val pts = if (isCorrect) quest.points else 0

            totalPoints += quest.points
            if (isCorrect) {
                correctCount++
                earnedPoints += pts
            } else if (selected == -1) {
                skippedCount++
            } else {
                wrongCount++
            }

            userAnswersList.add(
                UserAnswer(
                    questionId = quest.id,
                    selectedOptionIndex = selected,
                    isCorrect = isCorrect,
                    pointsEarned = pts
                )
            )
        }

        val percentage = if (totalPoints > 0) {
            ((earnedPoints.toDouble() / totalPoints) * 100).toInt()
        } else 0

        val result = QuizResult(
            quizId = q.id,
            quizTitle = q.title,
            totalQuestions = questions.size,
            correctCount = correctCount,
            wrongCount = wrongCount,
            skippedCount = skippedCount,
            earnedPoints = earnedPoints,
            totalPoints = totalPoints,
            percentage = percentage,
            timeTakenSeconds = _elapsedSeconds.value,
            userAnswers = userAnswersList,
            questions = questions
        )

        _quizResult.value = result

        viewModelScope.launch {
            repository.recordAttempt(result)
            val settings = settingsRepository.settings.value
            if (percentage >= 70) {
                soundManager.playCelebrationSound(settings.soundEffects)
                soundManager.triggerSuccessHaptic(settings.vibration)
            } else {
                soundManager.playWrongSound(settings.soundEffects)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
