package com.example.model

data class Question(
    val id: String,
    val question: String,
    val options: List<String>,
    val answer: Int, // 0-based index of the correct option
    val points: Int = 1,
    val explanation: String? = null
)

data class Quiz(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "General Knowledge",
    val difficulty: String = "Medium", // Easy, Medium, Hard, Custom
    val timeLimit: Int = 0, // In seconds (0 = unlimited)
    val shuffleQuestions: Boolean = true,
    val shuffleOptions: Boolean = true,
    val version: Int = 1,
    val questions: List<Question> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = 0L,
    val bestScore: Int = 0,
    val bestTotal: Int = 0,
    val attemptsCount: Int = 0
)

data class UserAnswer(
    val questionId: String,
    val selectedOptionIndex: Int, // -1 if skipped
    val isCorrect: Boolean,
    val pointsEarned: Int
)

data class QuizResult(
    val quizId: String,
    val quizTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val earnedPoints: Int,
    val totalPoints: Int,
    val percentage: Int,
    val timeTakenSeconds: Int,
    val userAnswers: List<UserAnswer>,
    val questions: List<Question>
)

data class SavedQuizProgress(
    val quizId: String,
    val currentQuestionIndex: Int,
    val selectedAnswers: Map<String, Int>, // questionId -> selectedOptionIndex
    val bookmarkedQuestionIds: Set<String> = emptySet(),
    val questionOrder: List<String>, // ordered list of questionIds
    val remainingTimeSeconds: Int,
    val elapsedSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class OverallStatistics(
    val totalQuizzes: Int = 0,
    val totalAttempts: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val overallAccuracy: Int = 0, // percentage 0-100
    val highestScorePercentage: Int = 0,
    val averageScorePercentage: Int = 0,
    val averageCompletionTimeSeconds: Int = 0
)
