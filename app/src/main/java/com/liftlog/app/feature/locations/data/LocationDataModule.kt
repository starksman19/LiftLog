package com.liftlog.app.feature.locations.data

import com.liftlog.app.feature.locations.domain.GymLocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationDataModule {
    @Binds
    @Singleton
    abstract fun bindGymLocationRepository(repository: RoomGymLocationRepository): GymLocationRepository
}
