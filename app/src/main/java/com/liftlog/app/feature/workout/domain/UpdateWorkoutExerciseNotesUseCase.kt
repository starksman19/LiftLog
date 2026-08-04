package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class UpdateWorkoutExerciseNotesUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutExerciseId: Long, notes: String?) =
        repository.updateWorkoutExerciseNotes(workoutExerciseId, notes)
}
