package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.RecentExercisePerformance
import javax.inject.Inject

class GetRecentExercisePerformancesUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(exerciseId: Long): List<RecentExercisePerformance> =
        repository.getRecentExercisePerformances(exerciseId)
}
