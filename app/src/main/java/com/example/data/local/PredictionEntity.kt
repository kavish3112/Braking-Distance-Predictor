package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val speedKmh: Double,
    val roadSurface: String,
    val weather: String,
    val tireCondition: String,
    val vehicleLoad: String,
    val roadSlope: String,
    val brakeCondition: String,
    val reactionTimeSeconds: Double,
    val brakingDistanceM: Double,
    val reactionDistanceM: Double,
    val totalStoppingDistanceM: Double,
    val stoppingTimeSeconds: Double,
    val decelerationMs2: Double,
    val safetyLevel: String,
    val isBookmarked: Boolean = false
)
