package com.liftlog.app.feature.exercises.domain

import javax.inject.Inject

class DeleteExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseId: Long) = repository.deleteExercise(exerciseId)
}
