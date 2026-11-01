package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedPredictions(): Flow<List<PredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity): Long

    @Update
    suspend fun updatePrediction(prediction: PredictionEntity)

    @Delete
    suspend fun deletePrediction(prediction: PredictionEntity)

    @Query("DELETE FROM predictions")
    suspend fun deleteAll()

    @Query("UPDATE predictions SET isBookmarked = NOT isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: Long)
}
