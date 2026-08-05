package com.liftlog.app.feature.report.data

import com.liftlog.app.feature.report.domain.TrainingReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrainingReportDataModule {
    @Binds
    @Singleton
    abstract fun bindTrainingReportRepository(
        repository: LocalTrainingReportRepository,
    ): TrainingReportRepository
}
