package com.example.data.repository

import com.example.data.local.PredictionDao
import com.example.data.local.PredictionEntity
import kotlinx.coroutines.flow.Flow

class PredictionRepository(private val predictionDao: PredictionDao) {
    val allPredictions: Flow<List<PredictionEntity>> = predictionDao.getAllPredictions()
    val bookmarkedPredictions: Flow<List<PredictionEntity>> = predictionDao.getBookmarkedPredictions()

    suspend fun savePrediction(entity: PredictionEntity): Long {
        return predictionDao.insertPrediction(entity)
    }

    suspend fun deletePrediction(entity: PredictionEntity) {
        predictionDao.deletePrediction(entity)
    }

    suspend fun toggleBookmark(id: Long) {
        predictionDao.toggleBookmark(id)
    }

    suspend fun clearAll() {
        predictionDao.deleteAll()
    }
}
