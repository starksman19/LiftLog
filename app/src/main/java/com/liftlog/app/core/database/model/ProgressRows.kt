package com.liftlog.app.core.database.model

data class SessionVolumeRow(
    val startedAtEpochMillis: Long,
    val volume: Double,
)

data class ExerciseProgressRow(
    val exerciseId: Long,
    val name: String,
    val lastPerformedAtEpochMillis: Long,
    val maxWeight: Double,
    val maxReps: Int,
    val totalVolume: Double,
    val workoutCount: Int,
)
