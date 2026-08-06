package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.WorkoutPlan
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutPlansUseCase @Inject constructor(
    private val repository: WorkoutTemplateRepository,
) {
    operator fun invoke(): Flow<List<WorkoutPlan>> = repository.observePlans()
}
