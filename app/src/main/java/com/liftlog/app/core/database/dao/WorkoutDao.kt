package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.model.WorkoutExerciseRow
import com.liftlog.app.core.database.model.RecentExercisePerformanceRow
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query(
        """
        SELECT *
        FROM workout_sessions
        WHERE finishedAtEpochMillis IS NULL
        ORDER BY startedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    fun observeActiveSession(): Flow<WorkoutSessionEntity?>

    @Query(
        """
        SELECT we.id AS workoutExerciseId,
               we.exerciseId AS exerciseId,
               e.name AS name,
               e.primaryMuscle AS primaryMuscle,
               e.equipment AS equipment,
               we.orderIndex AS orderIndex
        FROM workout_exercises AS we
        JOIN exercises AS e ON e.id = we.exerciseId
        WHERE we.workoutSessionId = :workoutSessionId
        ORDER BY we.orderIndex, we.id
        """,
    )
    fun observeWorkoutExercises(workoutSessionId: Long): Flow<List<WorkoutExerciseRow>>

    @Query(
        """
        SELECT *
        FROM set_entries
        WHERE workoutExerciseId IN (:workoutExerciseIds)
        ORDER BY workoutExerciseId, setNumber
        """,
    )
    fun observeSetEntries(workoutExerciseIds: List<Long>): Flow<List<SetEntryEntity>>

    @Query(
        """
        SELECT id
        FROM workout_sessions
        WHERE finishedAtEpochMillis IS NULL
        ORDER BY startedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getActiveSessionId(): Long?

    @Query(
        """
        SELECT ws.finishedAtEpochMillis AS finishedAtEpochMillis,
               se.setNumber AS setNumber,
               se.weight AS weight,
               se.reps AS reps
        FROM workout_sessions AS ws
        JOIN workout_exercises AS we ON we.workoutSessionId = ws.id
        JOIN set_entries AS se ON se.workoutExerciseId = we.id
        WHERE ws.id IN (
            SELECT ws2.id
            FROM workout_sessions AS ws2
            JOIN workout_exercises AS we2 ON we2.workoutSessionId = ws2.id
            WHERE ws2.finishedAtEpochMillis IS NOT NULL AND we2.exerciseId = :exerciseId
            ORDER BY ws2.finishedAtEpochMillis DESC
            LIMIT 2
        ) AND we.exerciseId = :exerciseId
        ORDER BY ws.finishedAtEpochMillis DESC, se.setNumber ASC
        """,
    )
    suspend fun getRecentExercisePerformances(exerciseId: Long): List<RecentExercisePerformanceRow>

    @Query(
        """
        SELECT exerciseId
        FROM workout_exercises
        WHERE workoutSessionId = :workoutSessionId
        ORDER BY orderIndex, id
        """,
    )
    suspend fun getExerciseIdsForWorkout(workoutSessionId: Long): List<Long>

    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM workout_exercises WHERE workoutSessionId = :workoutSessionId")
    suspend fun getNextExerciseOrder(workoutSessionId: Long): Int

    @Query("SELECT COALESCE(MAX(setNumber), 0) + 1 FROM set_entries WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun getNextSetNumber(workoutExerciseId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSession(entity: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercise(entity: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSetEntry(entity: SetEntryEntity): Long

    @Query(
        """
        UPDATE set_entries
        SET weight = :weight,
            reps = :reps,
            completedAtEpochMillis = :completedAtEpochMillis
        WHERE id = :setEntryId
        """,
    )
    suspend fun updateSetEntry(
        setEntryId: Long,
        weight: Double,
        reps: Int,
        completedAtEpochMillis: Long,
    )

    @Query("DELETE FROM set_entries WHERE id = :setEntryId")
    suspend fun deleteSetEntry(setEntryId: Long)

    @Query("UPDATE workout_sessions SET finishedAtEpochMillis = :finishedAtEpochMillis WHERE id = :workoutSessionId")
    suspend fun finishWorkout(workoutSessionId: Long, finishedAtEpochMillis: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :workoutSessionId")
    suspend fun deleteWorkout(workoutSessionId: Long)
}
