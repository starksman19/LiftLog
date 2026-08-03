package com.liftlog.app.feature.workout.data

import com.liftlog.app.feature.workout.domain.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkoutDataModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        repository: RoomWorkoutRepository,
    ): WorkoutRepository
}

