package com.liftlog.app.core.model

data class WorkoutSummary(
    val id: Long,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val notes: String?,
    val exerciseCount: Int,
    val volume: Double,
    val exerciseNames: String,
)

data class WorkoutDetail(
    val id: Long,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val notes: String?,
    val exercises: List<LoggedExercise>,
)
