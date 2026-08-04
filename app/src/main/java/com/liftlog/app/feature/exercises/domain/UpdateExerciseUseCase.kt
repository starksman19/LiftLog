package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.ExerciseDraft
import javax.inject.Inject

class UpdateExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseId: Long, draft: ExerciseDraft) {
        require(draft.name.isNotBlank()) { "Exercise name cannot be empty." }
        require(draft.primaryMuscle.isNotBlank()) { "Primary muscle cannot be empty." }
        require(draft.equipment.isNotBlank()) { "Equipment cannot be empty." }
        require(draft.category != ExerciseCategory.Machine || !draft.gymLocation.isNullOrBlank()) {
            "A machine exercise needs a gym location."
        }
        repository.updateExercise(exerciseId, draft)
    }
}
