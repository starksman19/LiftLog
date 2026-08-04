package com.liftlog.app.feature.progress.domain

import com.liftlog.app.core.model.HistoricalSet
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveExerciseHistoryUseCase @Inject constructor(
    private val repository: ProgressRepository,
) {
    operator fun invoke(exerciseId: Long): Flow<List<HistoricalSet>> = repository.observeExerciseHistory(exerciseId)
}
