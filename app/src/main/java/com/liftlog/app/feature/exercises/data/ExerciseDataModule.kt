package com.liftlog.app.feature.exercises.data

import com.liftlog.app.feature.exercises.domain.ExerciseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseDataModule {
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        repository: RoomExerciseRepository,
    ): ExerciseRepository
}

