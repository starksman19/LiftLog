package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.WorkoutTemplate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutTemplatesUseCase @Inject constructor(
    private val repository: WorkoutTemplateRepository,
) {
    operator fun invoke(): Flow<List<WorkoutTemplate>> = repository.observeTemplates()
}
