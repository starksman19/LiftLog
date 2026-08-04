package com.liftlog.app.feature.progress.domain

import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveProgressDashboardUseCase @Inject constructor(
    private val repository: ProgressRepository,
) {
    operator fun invoke(): Flow<ProgressDashboard> = combine(
        repository.observeRecentSessionVolumes(),
        repository.observeExerciseProgress(),
    ) { volumes, exercises ->
        ProgressDashboard(
            recentVolumes = volumes,
            exercises = exercises,
        )
    }
}

data class ProgressDashboard(
    val recentVolumes: List<SessionVolume>,
    val exercises: List<ExerciseProgress>,
)
