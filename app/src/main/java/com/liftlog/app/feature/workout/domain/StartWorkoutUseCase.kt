package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class StartWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(gymLocation: String?) {
        repository.startWorkout(gymLocation)
    }
}
