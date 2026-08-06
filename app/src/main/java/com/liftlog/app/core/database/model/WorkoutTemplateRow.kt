package com.liftlog.app.core.database.model

data class WorkoutTemplateRow(
    val id: Long,
    val name: String,
    val exerciseCount: Int,
    val planId: Long?,
    val planName: String?,
)
