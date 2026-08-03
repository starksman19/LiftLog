package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.ExerciseSearchEntity
import com.liftlog.app.core.database.entity.toSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT e.*
        FROM exercises AS e
        JOIN exercise_search AS s ON e.id = s.rowid
        WHERE exercise_search MATCH :query
        ORDER BY e.name
        """,
    )
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(entity: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSearch(entity: ExerciseSearchEntity)

    @Transaction
    suspend fun insertExerciseWithSearch(entity: ExerciseEntity): Long {
        val id = insertExercise(entity)
        insertExerciseSearch(entity.copy(id = id).toSearchEntity())
        return id
    }
}

