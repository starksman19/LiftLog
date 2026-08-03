package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class AddExerciseToActiveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(exerciseId: Long) {
        repository.addExerciseToActiveWorkout(exerciseId)
    }
}

