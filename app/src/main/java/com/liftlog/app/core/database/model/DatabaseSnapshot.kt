package com.liftlog.app.core.database.model

import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.GymLocationEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity

data class DatabaseSnapshot(
    val exercises: List<ExerciseEntity>,
    val workoutSessions: List<WorkoutSessionEntity>,
    val workoutExercises: List<WorkoutExerciseEntity>,
    val setEntries: List<SetEntryEntity>,
    val workoutTemplates: List<WorkoutTemplateEntity>,
    val workoutTemplateExercises: List<WorkoutTemplateExerciseEntity>,
    val gymLocations: List<GymLocationEntity> = emptyList(),
    val workoutPlans: List<WorkoutPlanEntity> = emptyList(),
)
