package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.Exercise
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    operator fun invoke(query: String): Flow<List<Exercise>> = repository.observeExercises(query)
}

