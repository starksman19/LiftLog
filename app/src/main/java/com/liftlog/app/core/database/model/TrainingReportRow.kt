package com.liftlog.app.core.database.model

data class TrainingReportRow(
    val workoutId: Long,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val workoutNotes: String?,
    val workoutExerciseId: Long?,
    val exerciseId: Long?,
    val exerciseName: String?,
    val exerciseCategory: String?,
    val primaryMuscle: String?,
    val equipment: String?,
    val exerciseNotes: String?,
    val exerciseOrderIndex: Int?,
    val setId: Long?,
    val setNumber: Int?,
    val weight: Double?,
    val reps: Int?,
    val completedAtEpochMillis: Long?,
)
