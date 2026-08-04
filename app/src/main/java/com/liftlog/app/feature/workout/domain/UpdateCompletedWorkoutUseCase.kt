package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class UpdateCompletedWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(
        workoutSessionId: Long,
        startedAtEpochMillis: Long,
        finishedAtEpochMillis: Long,
        gymLocation: String?,
        notes: String?,
    ) = repository.updateCompletedWorkoutDetails(
        workoutSessionId,
        startedAtEpochMillis,
        finishedAtEpochMillis,
        gymLocation,
        notes,
    )
}
