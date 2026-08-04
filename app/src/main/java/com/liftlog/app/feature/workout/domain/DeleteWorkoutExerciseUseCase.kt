package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class DeleteWorkoutExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutExerciseId: Long) = repository.deleteWorkoutExercise(workoutExerciseId)
}
