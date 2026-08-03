package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(query: String): Flow<List<Exercise>>
    suspend fun ensureStarterExercises()
}

