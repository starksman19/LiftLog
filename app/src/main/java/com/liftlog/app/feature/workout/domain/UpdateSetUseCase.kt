package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class UpdateSetUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(setEntryId: Long, weight: Double, reps: Int) {
        repository.updateSet(setEntryId, weight, reps)
    }
}
