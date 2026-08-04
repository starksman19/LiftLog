package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.liftlog.app.core.database.model.ExerciseProgressRow
import com.liftlog.app.core.database.model.SessionVolumeRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query(
        """
        SELECT ws.startedAtEpochMillis AS startedAtEpochMillis,
               COALESCE(SUM(se.weight * se.reps), 0) AS volume
        FROM workout_sessions AS ws
        LEFT JOIN workout_exercises AS we ON we.workoutSessionId = ws.id
        LEFT JOIN set_entries AS se ON se.workoutExerciseId = we.id
        WHERE ws.finishedAtEpochMillis IS NOT NULL
        GROUP BY ws.id
        ORDER BY ws.startedAtEpochMillis DESC
        LIMIT 7
        """,
    )
    fun observeRecentSessionVolumes(): Flow<List<SessionVolumeRow>>

    @Query(
        """
        SELECT e.id AS exerciseId,
               e.name AS name,
               MAX(ws.finishedAtEpochMillis) AS lastPerformedAtEpochMillis,
               MAX(se.weight) AS maxWeight,
               MAX(se.reps) AS maxReps,
               SUM(se.weight * se.reps) AS totalVolume,
               COUNT(DISTINCT ws.id) AS workoutCount
        FROM exercises AS e
        JOIN workout_exercises AS we ON we.exerciseId = e.id
        JOIN workout_sessions AS ws ON ws.id = we.workoutSessionId
        JOIN set_entries AS se ON se.workoutExerciseId = we.id
        WHERE ws.finishedAtEpochMillis IS NOT NULL
        GROUP BY e.id
        ORDER BY lastPerformedAtEpochMillis DESC
        """,
    )
    fun observeExerciseProgress(): Flow<List<ExerciseProgressRow>>
}
