package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class StartWorkoutTemplateUseCase @Inject constructor(
    private val repository: WorkoutTemplateRepository,
) {
    suspend operator fun invoke(templateId: Long, gymLocation: String?) {
        repository.startTemplate(templateId, gymLocation)
    }
}
