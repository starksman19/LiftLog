package com.liftlog.app.feature.locations.data

import com.liftlog.app.core.database.dao.GymLocationDao
import com.liftlog.app.core.database.entity.GymLocationEntity
import com.liftlog.app.feature.locations.domain.GymLocationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomGymLocationRepository @Inject constructor(
    private val gymLocationDao: GymLocationDao,
) : GymLocationRepository {
    override fun observeLocations(): Flow<List<String>> = gymLocationDao.observeLocations().map { rows -> rows.map { it.name } }
    override suspend fun add(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) gymLocationDao.insertLocation(GymLocationEntity(name = trimmed, createdAtEpochMillis = System.currentTimeMillis()))
    }
    override suspend fun rename(oldName: String, newName: String) = gymLocationDao.rename(oldName, newName)
    override suspend fun delete(name: String) = gymLocationDao.delete(name)
}
