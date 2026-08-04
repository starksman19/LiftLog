package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class DeleteCompletedWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutSessionId: Long) = repository.deleteCompletedWorkout(workoutSessionId)
}
