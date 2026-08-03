package com.liftlog.app.core.model

data class ActiveWorkout(
    val id: Long,
    val startedAtEpochMillis: Long,
    val exercises: List<LoggedExercise>,
)

data class LoggedExercise(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
    val orderIndex: Int,
    val sets: List<LoggedSet>,
)

data class LoggedSet(
    val id: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
)

