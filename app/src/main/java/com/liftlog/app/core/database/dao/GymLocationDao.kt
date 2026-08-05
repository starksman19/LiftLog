package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.GymLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GymLocationDao {
    @Query(
        """
        SELECT name FROM gym_locations
        ORDER BY name COLLATE NOCASE
        """,
    )
    fun observeLocations(): Flow<List<LocationRow>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(entity: GymLocationEntity)

    @Query("SELECT COUNT(*) FROM gym_locations WHERE name = :name COLLATE NOCASE")
    suspend fun locationCount(name: String): Int

    @Query("UPDATE gym_locations SET name = :newName WHERE name = :oldName COLLATE NOCASE")
    suspend fun updateLocationName(oldName: String, newName: String)

    @Query("DELETE FROM gym_locations WHERE name = :name COLLATE NOCASE")
    suspend fun deleteLocation(name: String)

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
        if (oldName.equals(newName, ignoreCase = true)) {
            updateLocationName(oldName, newName)
        } else {
            if (locationCount(newName) == 0) updateLocationName(oldName, newName)
            else deleteLocation(oldName)
        }
        renameExerciseLocations(oldName, newName)
        renameWorkoutLocations(oldName, newName)
    }

    @Transaction
    suspend fun delete(name: String) {
        clearExerciseLocations(name)
        clearWorkoutLocations(name)
        deleteLocation(name)
    }
}

data class LocationRow(val name: String)
