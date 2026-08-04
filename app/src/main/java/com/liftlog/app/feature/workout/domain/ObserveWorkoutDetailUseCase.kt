package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.WorkoutDetail
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutDetailUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    operator fun invoke(workoutSessionId: Long): Flow<WorkoutDetail?> = repository.observeWorkoutDetail(workoutSessionId)
}
