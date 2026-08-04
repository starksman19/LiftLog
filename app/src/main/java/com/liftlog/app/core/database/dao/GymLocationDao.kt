package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GymLocationDao {
    @Query(
        """
        SELECT gymLocation AS name FROM exercises
        WHERE gymLocation IS NOT NULL AND TRIM(gymLocation) != ''
        UNION
        SELECT gymLocation AS name FROM workout_sessions
        WHERE gymLocation IS NOT NULL AND TRIM(gymLocation) != ''
        ORDER BY name COLLATE NOCASE
        """,
    )
    fun observeLocations(): Flow<List<LocationRow>>

    @Query("UPDATE exercises SET gymLocation = :newName WHERE gymLocation = :oldName COLLATE NOCASE")
    suspend fun renameExerciseLocations(oldName: String, newName: String)

    @Query("UPDATE workout_sessions SET gymLocation = :newName WHERE gymLocation = :oldName COLLATE NOCASE")
    suspend fun renameWorkoutLocations(oldName: String, newName: String)

    @Query("UPDATE exercises SET gymLocation = NULL WHERE gymLocation = :name COLLATE NOCASE")
    suspend fun clearExerciseLocations(name: String)

    @Query("UPDATE workout_sessions SET gymLocation = NULL WHERE gymLocation = :name COLLATE NOCASE")
    suspend fun clearWorkoutLocations(name: String)

    @Transaction
    suspend fun rename(oldName: String, newName: String) {
        renameExerciseLocations(oldName, newName)
        renameWorkoutLocations(oldName, newName)
    }

    @Transaction
    suspend fun delete(name: String) {
        clearExerciseLocations(name)
        clearWorkoutLocations(name)
    }
}

data class LocationRow(val name: String)
