package com.liftlog.app.feature.exercises.domain

import javax.inject.Inject

class EnsureStarterExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke() {
        repository.ensureStarterExercises()
    }
}

