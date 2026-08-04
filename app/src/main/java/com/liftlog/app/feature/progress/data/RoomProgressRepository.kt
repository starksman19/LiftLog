package com.liftlog.app.feature.progress.data

import com.liftlog.app.core.database.dao.ProgressDao
import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import com.liftlog.app.core.model.HistoricalSet
import com.liftlog.app.feature.progress.domain.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomProgressRepository @Inject constructor(
    private val progressDao: ProgressDao,
) : ProgressRepository {
    override fun observeRecentSessionVolumes(): Flow<List<SessionVolume>> {
        return progressDao.observeRecentSessionVolumes().map { rows ->
            rows.map { row ->
                SessionVolume(
                    startedAtEpochMillis = row.startedAtEpochMillis,
                    volume = row.volume,
                )
            }.reversed()
        }
    }

    override fun observeExerciseProgress(): Flow<List<ExerciseProgress>> {
        return progressDao.observeExerciseProgress().map { rows ->
            rows.map { row ->
                ExerciseProgress(
                    exerciseId = row.exerciseId,
                    name = row.name,
                    lastPerformedAtEpochMillis = row.lastPerformedAtEpochMillis,
                    maxWeight = row.maxWeight,
                    maxReps = row.maxReps,
                    totalVolume = row.totalVolume,
                    workoutCount = row.workoutCount,
                )
            }
        }
    }

    override fun observeExerciseHistory(exerciseId: Long): Flow<List<HistoricalSet>> {
        return progressDao.observeExerciseHistory(exerciseId).map { rows ->
            rows.map { row ->
                HistoricalSet(
                    workoutSessionId = row.workoutSessionId,
                    finishedAtEpochMillis = row.finishedAtEpochMillis,
                    setEntryId = row.setEntryId,
                    setNumber = row.setNumber,
                    weight = row.weight,
                    reps = row.reps,
                )
            }
        }
    }
}
