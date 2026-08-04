package com.liftlog.app.feature.backup.data

import com.liftlog.app.feature.backup.domain.BackupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupDataModule {
    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        repository: LocalBackupRepository,
    ): BackupRepository
}
