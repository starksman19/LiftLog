package com.liftlog.app.feature.exercises.domain

import javax.inject.Inject

class AddCustomExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(name: String, primaryMuscle: String, equipment: String) {
        require(name.isNotBlank()) { "Exercise name cannot be empty." }
        require(primaryMuscle.isNotBlank()) { "Primary muscle cannot be empty." }
        require(equipment.isNotBlank()) { "Equipment cannot be empty." }
        repository.addCustomExercise(name.trim(), primaryMuscle.trim(), equipment.trim())
    }
}
