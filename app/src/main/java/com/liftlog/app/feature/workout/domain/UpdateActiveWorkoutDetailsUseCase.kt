package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class UpdateActiveWorkoutDetailsUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(gymLocation: String?, notes: String?) =
        repository.updateActiveWorkoutDetails(gymLocation, notes)
}
