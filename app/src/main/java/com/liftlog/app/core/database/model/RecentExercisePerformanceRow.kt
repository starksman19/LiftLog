package com.liftlog.app.core.database.model

data class RecentExercisePerformanceRow(
    val finishedAtEpochMillis: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
)
