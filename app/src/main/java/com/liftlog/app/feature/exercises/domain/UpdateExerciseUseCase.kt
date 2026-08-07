package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.ExerciseDraft
import javax.inject.Inject

class UpdateExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseId: Long, draft: ExerciseDraft) {
        require(draft.name.isNotBlank()) { "Exercise name cannot be empty." }
        repository.updateExercise(
            exerciseId,
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
