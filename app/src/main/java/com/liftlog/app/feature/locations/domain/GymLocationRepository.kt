package com.liftlog.app.feature.locations.domain

import kotlinx.coroutines.flow.Flow

interface GymLocationRepository {
    fun observeLocations(): Flow<List<String>>
    suspend fun rename(oldName: String, newName: String)
    suspend fun delete(name: String)
}
