package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class AddSetUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutExerciseId: Long, weight: Double, reps: Int) {
        repository.addSet(
            workoutExerciseId = workoutExerciseId,
            weight = weight,
            reps = reps,
        )
    }
}

