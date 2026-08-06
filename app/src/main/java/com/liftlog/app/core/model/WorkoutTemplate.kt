package com.liftlog.app.core.model

data class WorkoutTemplate(
    val id: Long,
    val name: String,
    val exerciseCount: Int,
    val planId: Long?,
    val planName: String?,
)
