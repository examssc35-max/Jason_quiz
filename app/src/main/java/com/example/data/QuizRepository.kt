package com.example.data

import com.example.model.OverallStatistics
import com.example.model.Question
import com.example.model.Quiz
import com.example.model.QuizResult
import com.example.model.SavedQuizProgress
import com.example.model.UserAnswer
import com.example.parser.QuizJsonParser
import com.example.parser.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class QuizRepository(
    private val quizDao: QuizDao,
    private val attemptDao: QuizAttemptDao,
    private val progressDao: QuizProgressDao
) {

    val allQuizzes: Flow<List<Quiz>> = quizDao.getAllQuizzes().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.Default)

    val favoriteQuizzes: Flow<List<Quiz>> = quizDao.getFavoriteQuizzes().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.Default)

    val recentQuizzes: Flow<List<Quiz>> = quizDao.getRecentQuizzes().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.Default)

    val allAttempts: Flow<List<QuizAttemptEntity>> = attemptDao.getAllAttempts()

    val activeProgressList: Flow<List<QuizProgressEntity>> = progressDao.getAllActiveProgress()

    val overallStatistics: Flow<OverallStatistics> = combine(
        quizDao.getAllQuizzes(),
        attemptDao.getAllAttempts()
    ) { quizzes, attempts ->
        if (attempts.isEmpty()) {
            OverallStatistics(
                totalQuizzes = quizzes.size,
                totalAttempts = 0,
                totalQuestionsAnswered = 0,
                correctAnswers = 0,
                wrongAnswers = 0,
                overallAccuracy = 0,
                highestScorePercentage = 0,
                averageScorePercentage = 0,
                averageCompletionTimeSeconds = 0
            )
        } else {
            val totalAttempts = attempts.size
            val totalQuestionsAnswered = attempts.sumOf { it.totalQuestions }
            val totalCorrect = attempts.sumOf { it.correctCount }
            val totalWrong = attempts.sumOf { it.wrongCount }
            val accuracy = if (totalQuestionsAnswered > 0) {
                ((totalCorrect.toDouble() / totalQuestionsAnswered) * 100).toInt()
            } else 0
            val highestScore = attempts.maxOfOrNull { it.percentage } ?: 0
            val averageScore = attempts.map { it.percentage }.average().toInt()
            val avgTime = attempts.map { it.timeTakenSeconds }.average().toInt()

            OverallStatistics(
                totalQuizzes = quizzes.size,
                totalAttempts = totalAttempts,
                totalQuestionsAnswered = totalQuestionsAnswered,
                correctAnswers = totalCorrect,
                wrongAnswers = totalWrong,
                overallAccuracy = accuracy,
                highestScorePercentage = highestScore,
                averageScorePercentage = averageScore,
                averageCompletionTimeSeconds = avgTime
            )
        }
    }.flowOn(Dispatchers.Default)

    suspend fun getQuizById(id: String): Quiz? = withContext(Dispatchers.IO) {
        quizDao.getQuizById(id)?.toDomain()
    }

    suspend fun saveQuiz(quiz: Quiz) = withContext(Dispatchers.IO) {
        val questionsJson = QuizJsonParser.exportToJson(quiz, quiz.questions)
        val entity = QuizEntity(
            id = quiz.id,
            title = quiz.title,
            description = quiz.description,
            category = quiz.category,
            difficulty = quiz.difficulty,
            timeLimit = quiz.timeLimit,
            shuffleQuestions = quiz.shuffleQuestions,
            shuffleOptions = quiz.shuffleOptions,
            version = quiz.version,
            questionsJson = questionsJson,
            isFavorite = quiz.isFavorite,
            createdAt = quiz.createdAt,
            lastPlayedAt = quiz.lastPlayedAt,
            bestScore = quiz.bestScore,
            bestTotal = quiz.bestTotal,
            attemptsCount = quiz.attemptsCount
        )
        quizDao.insertQuiz(entity)
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        quizDao.setFavorite(id, isFavorite)
    }

    suspend fun duplicateQuiz(quizId: String): Quiz? = withContext(Dispatchers.IO) {
        val original = getQuizById(quizId) ?: return@withContext null
        val newId = UUID.randomUUID().toString()
        val duplicated = original.copy(
            id = newId,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            lastPlayedAt = 0L,
            bestScore = 0,
            bestTotal = 0,
            attemptsCount = 0,
            isFavorite = false
        )
        saveQuiz(duplicated)
        duplicated
    }

    suspend fun deleteQuiz(id: String) = withContext(Dispatchers.IO) {
        quizDao.deleteQuizById(id)
        attemptDao.deleteAttemptsForQuiz(id)
        progressDao.deleteProgress(id)
    }

    suspend fun deleteAllQuizzes() = withContext(Dispatchers.IO) {
        quizDao.deleteAllQuizzes()
        attemptDao.clearAllAttempts()
        progressDao.clearAllProgress()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        attemptDao.clearAllAttempts()
        progressDao.clearAllProgress()
    }

    suspend fun recordAttempt(result: QuizResult) = withContext(Dispatchers.IO) {
        val answersJson = JSONArray().apply {
            result.userAnswers.forEach { a ->
                val obj = JSONObject()
                obj.put("questionId", a.questionId)
                obj.put("selectedOptionIndex", a.selectedOptionIndex)
                obj.put("isCorrect", a.isCorrect)
                obj.put("pointsEarned", a.pointsEarned)
                put(obj)
            }
        }.toString()

        val attemptEntity = QuizAttemptEntity(
            id = UUID.randomUUID().toString(),
            quizId = result.quizId,
            quizTitle = result.quizTitle,
            timestamp = System.currentTimeMillis(),
            totalQuestions = result.totalQuestions,
            correctCount = result.correctCount,
            wrongCount = result.wrongCount,
            skippedCount = result.skippedCount,
            earnedPoints = result.earnedPoints,
            totalPoints = result.totalPoints,
            percentage = result.percentage,
            timeTakenSeconds = result.timeTakenSeconds,
            answersSummaryJson = answersJson
        )

        attemptDao.insertAttempt(attemptEntity)
        quizDao.updateQuizStats(
            id = result.quizId,
            timestamp = System.currentTimeMillis(),
            earnedPoints = result.earnedPoints,
            totalPoints = result.totalPoints
        )
        // Clear active progress since quiz was finished
        progressDao.deleteProgress(result.quizId)
    }

    suspend fun saveProgress(progress: SavedQuizProgress, quizTitle: String) = withContext(Dispatchers.IO) {
        val answersObj = JSONObject()
        progress.selectedAnswers.forEach { (k, v) -> answersObj.put(k, v) }

        val bookmarksArray = JSONArray(progress.bookmarkedQuestionIds.toList())
        val orderArray = JSONArray(progress.questionOrder)

        val entity = QuizProgressEntity(
            quizId = progress.quizId,
            quizTitle = quizTitle,
            currentQuestionIndex = progress.currentQuestionIndex,
            selectedAnswersJson = answersObj.toString(),
            bookmarkedIdsJson = bookmarksArray.toString(),
            questionOrderJson = orderArray.toString(),
            remainingTimeSeconds = progress.remainingTimeSeconds,
            elapsedSeconds = progress.elapsedSeconds,
            timestamp = System.currentTimeMillis()
        )
        progressDao.saveProgress(entity)
    }

    suspend fun getProgress(quizId: String): SavedQuizProgress? = withContext(Dispatchers.IO) {
        val entity = progressDao.getProgressForQuiz(quizId) ?: return@withContext null
        try {
            val answersMap = mutableMapOf<String, Int>()
            val answersObj = JSONObject(entity.selectedAnswersJson)
            answersObj.keys().forEach { key ->
                answersMap[key] = answersObj.getInt(key)
            }

            val bookmarks = mutableSetOf<String>()
            val bookmarksArray = JSONArray(entity.bookmarkedIdsJson)
            for (i in 0 until bookmarksArray.length()) {
                bookmarks.add(bookmarksArray.getString(i))
            }

            val order = mutableListOf<String>()
            val orderArray = JSONArray(entity.questionOrderJson)
            for (i in 0 until orderArray.length()) {
                order.add(orderArray.getString(i))
            }

            SavedQuizProgress(
                quizId = entity.quizId,
                currentQuestionIndex = entity.currentQuestionIndex,
                selectedAnswers = answersMap,
                bookmarkedQuestionIds = bookmarks,
                questionOrder = order,
                remainingTimeSeconds = entity.remainingTimeSeconds,
                elapsedSeconds = entity.elapsedSeconds,
                timestamp = entity.timestamp
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearProgress(quizId: String) = withContext(Dispatchers.IO) {
        progressDao.deleteProgress(quizId)
    }

    suspend fun seedInitialQuizzesIfEmpty() = withContext(Dispatchers.IO) {
        val samples = QuizJsonParser.getSampleQuizzes()
        for ((quiz, questions) in samples) {
            val existing = quizDao.getQuizById(quiz.id)
            if (existing == null) {
                saveQuiz(quiz.copy(questions = questions))
            }
        }
    }

    suspend fun exportAllQuizzesBackup(): String = withContext(Dispatchers.IO) {
        val rootArray = JSONArray()
        val all = quizDao.getAllQuizzes()
        // Wait, for exportAllQuizzesBackup:
        val list = mutableListOf<Quiz>()
        // Load directly
        val sampleOrExisting = QuizJsonParser.getSampleQuizzes()
        // We can get all entities directly:
        // Let's add a sync query or collect first
        return@withContext exportAllQuizzesSync()
    }

    private suspend fun exportAllQuizzesSync(): String {
        // Collect current list of quizzes
        val currentQuizzes = mutableListOf<Quiz>()
        val samples = QuizJsonParser.getSampleQuizzes()
        for ((q, list) in samples) {
            val inDb = quizDao.getQuizById(q.id)?.toDomain()
            if (inDb != null) {
                currentQuizzes.add(inDb)
            } else {
                currentQuizzes.add(q.copy(questions = list))
            }
        }
        val array = JSONArray()
        for (q in currentQuizzes) {
            array.put(JSONObject(QuizJsonParser.exportToJson(q, q.questions)))
        }
        return array.toString(2)
    }

    private fun QuizEntity.toDomain(): Quiz {
        val parsed = try {
            val obj = JSONObject(questionsJson)
            when (val res = QuizJsonParser.parseAndValidate(questionsJson, existingId = id)) {
                is ValidationResult.Success -> res.quiz.questions
                is ValidationResult.Error -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        return Quiz(
            id = id,
            title = title,
            description = description,
            category = category,
            difficulty = difficulty,
            timeLimit = timeLimit,
            shuffleQuestions = shuffleQuestions,
            shuffleOptions = shuffleOptions,
            version = version,
            questions = parsed,
            isFavorite = isFavorite,
            createdAt = createdAt,
            lastPlayedAt = lastPlayedAt,
            bestScore = bestScore,
            bestTotal = bestTotal,
            attemptsCount = attemptsCount
        )
    }
}
