package com.liftlog.app.core.model

data class Exercise(
    val id: Long,
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
    val isCustom: Boolean,
)

