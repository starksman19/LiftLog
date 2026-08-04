package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.WorkoutSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCompletedWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    operator fun invoke(): Flow<List<WorkoutSummary>> = repository.observeCompletedWorkouts()
}
