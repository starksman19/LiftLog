package com.liftlog.app.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.liftlog.app.core.database.dao.ExerciseDao
import com.liftlog.app.core.database.dao.BackupDao
import com.liftlog.app.core.database.dao.ProgressDao
import com.liftlog.app.core.database.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): LiftLogDatabase = Room.databaseBuilder(
        context = context,
        klass = LiftLogDatabase::class.java,
        name = "liftlog.db",
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun provideExerciseDao(database: LiftLogDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideBackupDao(database: LiftLogDatabase): BackupDao = database.backupDao()

    @Provides
    fun provideProgressDao(database: LiftLogDatabase): ProgressDao = database.progressDao()

    @Provides
    fun provideWorkoutDao(database: LiftLogDatabase): WorkoutDao = database.workoutDao()
}
