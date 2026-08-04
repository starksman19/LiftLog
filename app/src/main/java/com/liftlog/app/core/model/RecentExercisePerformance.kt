package com.liftlog.app.core.model

data class RecentExercisePerformance(
    val finishedAtEpochMillis: Long,
    val sets: List<LoggedSet>,
)
