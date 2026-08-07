package com.liftlog.app.core.database.model

data class RecentExercisePerformanceRow(
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val category: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
)
