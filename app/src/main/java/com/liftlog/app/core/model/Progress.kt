package com.liftlog.app.core.model

data class SessionVolume(
    val startedAtEpochMillis: Long,
    val volume: Double,
)

data class ExerciseProgress(
    val exerciseId: Long,
    val name: String,
    val lastPerformedAtEpochMillis: Long,
    val maxWeight: Double,
    val maxReps: Int,
    val totalVolume: Double,
    val workoutCount: Int,
)

data class HistoricalSet(
    val workoutSessionId: Long,
    val finishedAtEpochMillis: Long,
    val setEntryId: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
)
