package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.ExerciseDraft
import javax.inject.Inject

class UpdateExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseId: Long, draft: ExerciseDraft) {
        require(draft.name.isNotBlank()) { "Exercise name cannot be empty." }
        require(draft.category != ExerciseCategory.Machine || !draft.gymLocation.isNullOrBlank()) {
            "A machine exercise needs a gym location."
        }
        repository.updateExercise(
            exerciseId,
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
