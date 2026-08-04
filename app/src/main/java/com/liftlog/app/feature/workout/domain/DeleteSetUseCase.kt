package com.liftlog.app.feature.workout.domain

import javax.inject.Inject

class DeleteSetUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(setEntryId: Long) {
        repository.deleteSet(setEntryId)
    }
}
