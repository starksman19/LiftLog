package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.RecentExercisePerformance
import com.liftlog.app.core.model.WorkoutDetail
import com.liftlog.app.core.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveWorkout(): Flow<ActiveWorkout?>
    fun observeCompletedWorkouts(): Flow<List<WorkoutSummary>>
    fun observeWorkoutDetail(workoutSessionId: Long): Flow<WorkoutDetail?>
    suspend fun startWorkout(gymLocation: String?)
    suspend fun getRecentExercisePerformances(exerciseId: Long): List<RecentExercisePerformance>
    suspend fun addExerciseToActiveWorkout(exerciseId: Long, notes: String?)
    suspend fun addExerciseToWorkout(workoutSessionId: Long, exerciseId: Long, notes: String?)
    suspend fun addSet(workoutExerciseId: Long, weight: Double, reps: Int)
    suspend fun updateSet(setEntryId: Long, weight: Double, reps: Int)
    suspend fun deleteSet(setEntryId: Long)
    suspend fun updateActiveWorkoutDetails(gymLocation: String?, notes: String?)
    suspend fun updateWorkoutExerciseNotes(workoutExerciseId: Long, notes: String?)
    suspend fun deleteWorkoutExercise(workoutExerciseId: Long)
    suspend fun updateCompletedWorkoutDetails(
        workoutSessionId: Long,
        startedAtEpochMillis: Long,
        finishedAtEpochMillis: Long,
        gymLocation: String?,
        notes: String?,
    )
    suspend fun deleteCompletedWorkout(workoutSessionId: Long)
    suspend fun finishActiveWorkout()
    suspend fun discardActiveWorkout()
}
