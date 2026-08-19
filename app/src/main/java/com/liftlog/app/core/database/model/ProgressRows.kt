package com.liftlog.app.core.database.model

data class SessionVolumeRow(
    val startedAtEpochMillis: Long,
    val volume: Double,
    val setCount: Int,
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

data class ExerciseHistoryRow(
    val workoutSessionId: Long,
    val finishedAtEpochMillis: Long,
    val setEntryId: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
)
