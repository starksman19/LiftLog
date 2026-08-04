package com.liftlog.app.core.database.model

data class WorkoutSummaryRow(
    val id: Long,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val notes: String?,
    val exerciseCount: Int,
    val volume: Double,
    val exerciseNames: String,
)
