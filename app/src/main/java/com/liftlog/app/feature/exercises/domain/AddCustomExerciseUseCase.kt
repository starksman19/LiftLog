package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.ExerciseDraft
import javax.inject.Inject

class AddCustomExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(draft: ExerciseDraft): Long {
        require(draft.name.isNotBlank()) { "Exercise name cannot be empty." }
        return repository.addCustomExercise(
            draft.copy(
                name = draft.name.trim(),
                primaryMuscle = draft.primaryMuscle.trim(),
                equipment = draft.equipment.trim(),
                gymLocation = null,
                youTubeUrl = draft.youTubeUrl?.trim()?.takeIf { it.isNotEmpty() },
                imageUri = draft.imageUri?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }
}
