package com.liftlog.app.feature.progress.domain

import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import com.liftlog.app.core.model.HistoricalSet
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeRecentSessionVolumes(limit: Int): Flow<List<SessionVolume>>
    fun observeExerciseProgress(): Flow<List<ExerciseProgress>>
    fun observeExerciseHistory(exerciseId: Long): Flow<List<HistoricalSet>>
}
