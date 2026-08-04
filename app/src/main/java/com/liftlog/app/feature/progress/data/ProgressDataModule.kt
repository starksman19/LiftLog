package com.liftlog.app.feature.progress.data

import com.liftlog.app.feature.progress.domain.ProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressDataModule {
    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        repository: RoomProgressRepository,
    ): ProgressRepository
}
