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
import com.liftlog.app.core.database.model.WorkoutSummaryRow
import com.liftlog.app.core.database.model.TrainingReportRow
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
        SELECT ws.id AS id,
               ws.startedAtEpochMillis AS startedAtEpochMillis,
               ws.finishedAtEpochMillis AS finishedAtEpochMillis,
               ws.gymLocation AS gymLocation,
               ws.notes AS notes,
               COUNT(DISTINCT we.id) AS exerciseCount,
               COALESCE(SUM(se.weight * se.reps), 0.0) AS volume,
               COALESCE(GROUP_CONCAT(DISTINCT e.name), '') AS exerciseNames
        FROM workout_sessions AS ws
        LEFT JOIN workout_exercises AS we ON we.workoutSessionId = ws.id
        LEFT JOIN exercises AS e ON e.id = we.exerciseId
        LEFT JOIN set_entries AS se ON se.workoutExerciseId = we.id
        WHERE ws.finishedAtEpochMillis IS NOT NULL
        GROUP BY ws.id
        ORDER BY ws.finishedAtEpochMillis DESC
        """,
    )
    fun observeCompletedWorkoutSummaries(): Flow<List<WorkoutSummaryRow>>

    @Query("SELECT * FROM workout_sessions WHERE id = :workoutSessionId LIMIT 1")
    fun observeWorkoutSession(workoutSessionId: Long): Flow<WorkoutSessionEntity?>

    @Query(
        """
        SELECT we.id AS workoutExerciseId,
               we.exerciseId AS exerciseId,
               e.name AS name,
               e.primaryMuscle AS primaryMuscle,
               e.equipment AS equipment,
               we.orderIndex AS orderIndex,
               we.notes AS notes
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
               ws.gymLocation AS gymLocation,
               e.category AS category,
               se.setNumber AS setNumber,
               se.weight AS weight,
               se.reps AS reps
        FROM workout_sessions AS ws
        JOIN workout_exercises AS we ON we.workoutSessionId = ws.id
        JOIN exercises AS e ON e.id = we.exerciseId
        JOIN set_entries AS se ON se.workoutExerciseId = we.id
        WHERE ws.finishedAtEpochMillis IS NOT NULL
          AND we.exerciseId = :exerciseId
        ORDER BY ws.finishedAtEpochMillis DESC, se.setNumber ASC
        """,
    )
    suspend fun getRecentExercisePerformances(exerciseId: Long): List<RecentExercisePerformanceRow>

    @Query(
        """
        SELECT ws.id AS workoutId,
               ws.startedAtEpochMillis AS startedAtEpochMillis,
               ws.finishedAtEpochMillis AS finishedAtEpochMillis,
               ws.gymLocation AS gymLocation,
               ws.notes AS workoutNotes,
               we.id AS workoutExerciseId,
               e.id AS exerciseId,
               e.name AS exerciseName,
               e.category AS exerciseCategory,
               e.primaryMuscle AS primaryMuscle,
               e.equipment AS equipment,
               we.notes AS exerciseNotes,
               we.orderIndex AS exerciseOrderIndex,
               se.id AS setId,
               se.setNumber AS setNumber,
               se.weight AS weight,
               se.reps AS reps,
               se.completedAtEpochMillis AS completedAtEpochMillis
        FROM workout_sessions AS ws
        LEFT JOIN workout_exercises AS we ON we.workoutSessionId = ws.id
        LEFT JOIN exercises AS e ON e.id = we.exerciseId
        LEFT JOIN set_entries AS se ON se.workoutExerciseId = we.id
        WHERE ws.finishedAtEpochMillis >= :startEpochMillis
          AND ws.finishedAtEpochMillis < :endExclusiveEpochMillis
        ORDER BY ws.finishedAtEpochMillis ASC, we.orderIndex ASC, se.setNumber ASC
        """,
    )
    suspend fun getTrainingReportRows(
        startEpochMillis: Long,
        endExclusiveEpochMillis: Long,
    ): List<TrainingReportRow>

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

    @Query("UPDATE workout_sessions SET gymLocation = :gymLocation, notes = :notes WHERE id = :workoutSessionId")
    suspend fun updateWorkoutDetails(workoutSessionId: Long, gymLocation: String?, notes: String?)

    @Query("UPDATE workout_exercises SET notes = :notes WHERE id = :workoutExerciseId")
    suspend fun updateWorkoutExerciseNotes(workoutExerciseId: Long, notes: String?)

    @Query("DELETE FROM workout_exercises WHERE id = :workoutExerciseId")
    suspend fun deleteWorkoutExercise(workoutExerciseId: Long)

    @Query(
        """
        UPDATE workout_sessions
        SET startedAtEpochMillis = :startedAtEpochMillis,
            finishedAtEpochMillis = :finishedAtEpochMillis,
            gymLocation = :gymLocation,
            notes = :notes
        WHERE id = :workoutSessionId
        """,
    )
    suspend fun updateCompletedWorkoutDetails(
        workoutSessionId: Long,
        startedAtEpochMillis: Long,
        finishedAtEpochMillis: Long,
        gymLocation: String?,
        notes: String?,
    )

    @Query("UPDATE workout_sessions SET finishedAtEpochMillis = :finishedAtEpochMillis WHERE id = :workoutSessionId")
    suspend fun finishWorkout(workoutSessionId: Long, finishedAtEpochMillis: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :workoutSessionId")
    suspend fun deleteWorkout(workoutSessionId: Long)
}
