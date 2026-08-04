package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class AddExerciseToWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutSessionId: Long, exerciseId: Long, notes: String?) =
        repository.addExerciseToWorkout(workoutSessionId, exerciseId, notes)
}
