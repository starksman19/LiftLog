package com.liftlog.app.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.liftlog.app.core.model.Exercise

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["name"]),
        Index(value = ["primaryMuscle"]),
        Index(value = ["equipment"]),
    ],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
    val isCustom: Boolean = false,
    val createdAtEpochMillis: Long,
)

fun ExerciseEntity.toModel(): Exercise = Exercise(
    id = id,
    name = name,
    primaryMuscle = primaryMuscle,
    equipment = equipment,
    isCustom = isCustom,
)

