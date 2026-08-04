package com.liftlog.app.core.model

data class Exercise(
    val id: Long,
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
    val category: ExerciseCategory,
    val gymLocation: String?,
    val youTubeUrl: String?,
    val imageUri: String?,
    val isCustom: Boolean,
)
