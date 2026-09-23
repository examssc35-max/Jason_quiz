package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC")
    fun getRecentQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: String): QuizEntity?

    @Query("SELECT COUNT(*) FROM quizzes")
    fun getQuizCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Query("UPDATE quizzes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE quizzes SET lastPlayedAt = :timestamp, bestScore = CASE WHEN :earnedPoints > bestScore THEN :earnedPoints ELSE bestScore END, bestTotal = :totalPoints, attemptsCount = attemptsCount + 1 WHERE id = :id")
    suspend fun updateQuizStats(id: String, timestamp: Long, earnedPoints: Int, totalPoints: Int)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteQuizById(id: String)

    @Query("DELETE FROM quizzes")
    suspend fun deleteAllQuizzes()
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId ORDER BY timestamp DESC")
    fun getAttemptsForQuiz(quizId: String): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Query("DELETE FROM quiz_attempts WHERE quizId = :quizId")
    suspend fun deleteAttemptsForQuiz(quizId: String)

    @Query("DELETE FROM quiz_attempts")
    suspend fun clearAllAttempts()
}

@Dao
interface QuizProgressDao {
    @Query("SELECT * FROM quiz_progress ORDER BY timestamp DESC")
    fun getAllActiveProgress(): Flow<List<QuizProgressEntity>>

    @Query("SELECT * FROM quiz_progress WHERE quizId = :quizId")
    suspend fun getProgressForQuiz(quizId: String): QuizProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: QuizProgressEntity)

    @Query("DELETE FROM quiz_progress WHERE quizId = :quizId")
    suspend fun deleteProgress(quizId: String)

    @Query("DELETE FROM quiz_progress")
    suspend fun clearAllProgress()
}
