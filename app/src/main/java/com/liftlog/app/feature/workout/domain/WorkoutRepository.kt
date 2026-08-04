package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.ActiveWorkout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveWorkout(): Flow<ActiveWorkout?>
    suspend fun startWorkout()
    suspend fun addExerciseToActiveWorkout(exerciseId: Long)
    suspend fun addSet(workoutExerciseId: Long, weight: Double, reps: Int)
    suspend fun updateSet(setEntryId: Long, weight: Double, reps: Int)
    suspend fun deleteSet(setEntryId: Long)
    suspend fun finishActiveWorkout()
    suspend fun discardActiveWorkout()
}
