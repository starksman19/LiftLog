package com.liftlog.app.core.database.model

import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity

data class DatabaseSnapshot(
    val exercises: List<ExerciseEntity>,
    val workoutSessions: List<WorkoutSessionEntity>,
    val workoutExercises: List<WorkoutExerciseEntity>,
    val setEntries: List<SetEntryEntity>,
)
