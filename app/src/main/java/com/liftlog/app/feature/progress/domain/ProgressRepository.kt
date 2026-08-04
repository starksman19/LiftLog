package com.liftlog.app.feature.progress.domain

import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeRecentSessionVolumes(): Flow<List<SessionVolume>>
    fun observeExerciseProgress(): Flow<List<ExerciseProgress>>
}
