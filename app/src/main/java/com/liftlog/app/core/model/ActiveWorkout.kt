package com.liftlog.app.core.model

data class ActiveWorkout(
    val id: Long,
    val startedAtEpochMillis: Long,
    val gymLocation: String?,
    val notes: String?,
    val exercises: List<LoggedExercise>,
)

data class LoggedExercise(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val category: ExerciseCategory,
    val primaryMuscle: String,
    val equipment: String,
    val orderIndex: Int,
    val notes: String?,
    val sets: List<LoggedSet>,
)

data class LoggedSet(
    val id: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val completedAtEpochMillis: Long = 0,
)
