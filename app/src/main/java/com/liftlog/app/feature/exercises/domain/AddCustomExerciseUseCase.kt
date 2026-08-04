package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.ExerciseDraft
import javax.inject.Inject

class AddCustomExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(draft: ExerciseDraft) {
        require(draft.name.isNotBlank()) { "Exercise name cannot be empty." }
        require(draft.primaryMuscle.isNotBlank()) { "Primary muscle cannot be empty." }
        require(draft.equipment.isNotBlank()) { "Equipment cannot be empty." }
        require(draft.category != ExerciseCategory.Machine || !draft.gymLocation.isNullOrBlank()) {
            "A machine exercise needs a gym location."
        }
        repository.addCustomExercise(
            draft.copy(
                name = draft.name.trim(),
                primaryMuscle = draft.primaryMuscle.trim(),
                equipment = draft.equipment.trim(),
                gymLocation = draft.gymLocation?.trim()?.takeIf { it.isNotEmpty() },
                youTubeUrl = draft.youTubeUrl?.trim()?.takeIf { it.isNotEmpty() },
                imageUri = draft.imageUri?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }
}
