package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.ActiveWorkout
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveActiveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    operator fun invoke(): Flow<ActiveWorkout?> = repository.observeActiveWorkout()
}

