package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val timeLimit: Int,
    val shuffleQuestions: Boolean,
    val shuffleOptions: Boolean,
    val version: Int,
    val questionsJson: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = 0L,
    val bestScore: Int = 0,
    val bestTotal: Int = 0,
    val attemptsCount: Int = 0
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val quizTitle: String,
    val timestamp: Long,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val earnedPoints: Int,
    val totalPoints: Int,
    val percentage: Int,
    val timeTakenSeconds: Int,
    val answersSummaryJson: String
)

@Entity(tableName = "quiz_progress")
data class QuizProgressEntity(
    @PrimaryKey val quizId: String,
    val quizTitle: String,
    val currentQuestionIndex: Int,
    val selectedAnswersJson: String,
    val bookmarkedIdsJson: String,
    val questionOrderJson: String,
    val remainingTimeSeconds: Int,
    val elapsedSeconds: Int,
    val timestamp: Long
)
