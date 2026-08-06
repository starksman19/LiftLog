package com.liftlog.app.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index

@Entity(
    tableName = "workout_template_plans",
    primaryKeys = ["templateId", "planId"],
    indices = [Index(value = ["planId"])],
)
data class WorkoutTemplatePlanEntity(
    val templateId: Long,
    val planId: Long,
)
