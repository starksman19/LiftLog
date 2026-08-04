package com.liftlog.app.core.database.model

data class WorkoutExerciseRow(
    val workoutExerciseId: Long,
    val exerciseId: Long,
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
    val orderIndex: Int,
    val notes: String?,
)
