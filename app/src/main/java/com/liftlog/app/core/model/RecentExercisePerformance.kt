package com.liftlog.app.core.model

data class RecentExercisePerformance(
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val category: ExerciseCategory,
    val sets: List<LoggedSet>,
)
