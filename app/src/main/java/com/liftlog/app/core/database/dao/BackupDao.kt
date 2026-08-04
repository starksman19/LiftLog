package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.ExerciseSearchEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.entity.toSearchEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot

@Dao
interface BackupDao {
    @Query("SELECT * FROM exercises ORDER BY id")
    suspend fun getExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY id")
    suspend fun getWorkoutSessions(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_exercises ORDER BY id")
    suspend fun getWorkoutExercises(): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM set_entries ORDER BY id")
    suspend fun getSetEntries(): List<SetEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercises(entities: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExerciseSearch(entities: List<ExerciseSearchEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSessions(entities: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercises(entities: List<WorkoutExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSetEntries(entities: List<SetEntryEntity>)

    @Query("DELETE FROM set_entries")
    suspend fun clearSetEntries()

    @Query("DELETE FROM workout_exercises")
    suspend fun clearWorkoutExercises()

    @Query("DELETE FROM workout_sessions")
    suspend fun clearWorkoutSessions()

    @Query("DELETE FROM exercise_search")
    suspend fun clearExerciseSearch()

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Transaction
    suspend fun snapshot(): DatabaseSnapshot = DatabaseSnapshot(
        exercises = getExercises(),
        workoutSessions = getWorkoutSessions(),
        workoutExercises = getWorkoutExercises(),
        setEntries = getSetEntries(),
    )

    @Transaction
    suspend fun replaceAll(snapshot: DatabaseSnapshot) {
        clearSetEntries()
        clearWorkoutExercises()
        clearWorkoutSessions()
        clearExerciseSearch()
        clearExercises()

        if (snapshot.exercises.isNotEmpty()) {
            insertExercises(snapshot.exercises)
            insertExerciseSearch(snapshot.exercises.map(ExerciseEntity::toSearchEntity))
        }
        if (snapshot.workoutSessions.isNotEmpty()) insertWorkoutSessions(snapshot.workoutSessions)
        if (snapshot.workoutExercises.isNotEmpty()) insertWorkoutExercises(snapshot.workoutExercises)
        if (snapshot.setEntries.isNotEmpty()) insertSetEntries(snapshot.setEntries)
    }
}
