package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class SaveActiveWorkoutAsTemplateUseCase @Inject constructor(
    private val repository: WorkoutTemplateRepository,
) {
    suspend operator fun invoke(name: String) {
        require(name.isNotBlank()) { "Template name cannot be empty." }
        repository.saveActiveWorkoutAsTemplate(name.trim())
    }
}
