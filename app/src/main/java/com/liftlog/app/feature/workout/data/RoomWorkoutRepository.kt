package com.liftlog.app.feature.workout.data

import com.liftlog.app.core.database.dao.WorkoutDao
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.model.WorkoutExerciseRow
import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.LoggedExercise
import com.liftlog.app.core.model.LoggedSet
import com.liftlog.app.core.model.RecentExercisePerformance
import com.liftlog.app.feature.workout.domain.WorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RoomWorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
) : WorkoutRepository {
    override fun observeActiveWorkout(): Flow<ActiveWorkout?> {
        return workoutDao.observeActiveSession().flatMapLatest { session ->
            if (session == null) {
                flowOf(null)
            } else {
                workoutDao.observeWorkoutExercises(session.id).flatMapLatest { exerciseRows ->
                    val exerciseIds = exerciseRows.map { it.workoutExerciseId }
                    if (exerciseIds.isEmpty()) {
                        flowOf(
                            ActiveWorkout(
                id = session.id,
                startedAtEpochMillis = session.startedAtEpochMillis,
                gymLocation = session.gymLocation,
                                exercises = emptyList(),
                            ),
                        )
                    } else {
                        workoutDao.observeSetEntries(exerciseIds).map { setEntries ->
                            session.toActiveWorkout(exerciseRows, setEntries)
                        }
                    }
                }
            }
        }
    }

    override suspend fun startWorkout(gymLocation: String?) {
        if (workoutDao.getActiveSessionId() != null) return

        workoutDao.insertWorkoutSession(
            WorkoutSessionEntity(
                startedAtEpochMillis = System.currentTimeMillis(),
                gymLocation = gymLocation?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    override suspend fun getRecentExercisePerformances(exerciseId: Long): List<RecentExercisePerformance> {
        return workoutDao.getRecentExercisePerformances(exerciseId)
            .groupBy { it.finishedAtEpochMillis }
            .map { (finishedAt, rows) ->
                RecentExercisePerformance(
                    finishedAtEpochMillis = finishedAt,
                    sets = rows.map { row ->
                        LoggedSet(
                            id = 0,
                            setNumber = row.setNumber,
                            weight = row.weight,
                            reps = row.reps,
                        )
                    },
                )
            }
    }

    override suspend fun addExerciseToActiveWorkout(exerciseId: Long, notes: String?) {
        val workoutSessionId = workoutDao.getActiveSessionId() ?: return
        val orderIndex = workoutDao.getNextExerciseOrder(workoutSessionId)

        workoutDao.insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutSessionId = workoutSessionId,
                exerciseId = exerciseId,
                orderIndex = orderIndex,
                notes = notes?.takeIf { it.isNotBlank() },
            ),
        )
    }

    override suspend fun addSet(workoutExerciseId: Long, weight: Double, reps: Int) {
        require(weight >= 0) { "Weight cannot be negative." }
        require(reps > 0) { "Reps must be greater than zero." }

        val setNumber = workoutDao.getNextSetNumber(workoutExerciseId)
        workoutDao.insertSetEntry(
            SetEntryEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = setNumber,
                weight = weight,
                reps = reps,
                completedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun updateSet(setEntryId: Long, weight: Double, reps: Int) {
        require(weight >= 0) { "Weight cannot be negative." }
        require(reps > 0) { "Reps must be greater than zero." }

        workoutDao.updateSetEntry(
            setEntryId = setEntryId,
            weight = weight,
            reps = reps,
            completedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    override suspend fun deleteSet(setEntryId: Long) {
        workoutDao.deleteSetEntry(setEntryId)
    }

    override suspend fun finishActiveWorkout() {
        val workoutSessionId = workoutDao.getActiveSessionId() ?: return
        workoutDao.finishWorkout(
            workoutSessionId = workoutSessionId,
            finishedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    override suspend fun discardActiveWorkout() {
        val workoutSessionId = workoutDao.getActiveSessionId() ?: return
        workoutDao.deleteWorkout(workoutSessionId)
    }

    private fun WorkoutSessionEntity.toActiveWorkout(
        exerciseRows: List<WorkoutExerciseRow>,
        setEntries: List<SetEntryEntity>,
    ): ActiveWorkout {
        val setsByExercise = setEntries.groupBy { it.workoutExerciseId }

        return ActiveWorkout(
            id = id,
            startedAtEpochMillis = startedAtEpochMillis,
            gymLocation = gymLocation,
            exercises = exerciseRows.map { exerciseRow ->
                LoggedExercise(
                    id = exerciseRow.workoutExerciseId,
                    exerciseId = exerciseRow.exerciseId,
                    name = exerciseRow.name,
                    primaryMuscle = exerciseRow.primaryMuscle,
                    equipment = exerciseRow.equipment,
                    orderIndex = exerciseRow.orderIndex,
                    sets = setsByExercise[exerciseRow.workoutExerciseId]
                        .orEmpty()
                        .map { setEntry ->
                            LoggedSet(
                                id = setEntry.id,
                                setNumber = setEntry.setNumber,
                                weight = setEntry.weight,
                                reps = setEntry.reps,
                            )
                        },
                )
            },
        )
    }
}
