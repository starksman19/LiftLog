package com.liftlog.app.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Fts5
import androidx.room3.PrimaryKey

@Fts5
@Entity(tableName = "exercise_search")
data class ExerciseSearchEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Long,
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
)

fun ExerciseEntity.toSearchEntity(): ExerciseSearchEntity = ExerciseSearchEntity(
    rowId = id,
    name = name,
    primaryMuscle = primaryMuscle,
    equipment = equipment,
)

