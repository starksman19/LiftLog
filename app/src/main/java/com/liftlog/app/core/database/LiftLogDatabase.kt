package com.liftlog.app.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.liftlog.app.core.database.dao.ExerciseDao
import com.liftlog.app.core.database.dao.BackupDao
import com.liftlog.app.core.database.dao.ProgressDao
import com.liftlog.app.core.database.dao.WorkoutDao
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.ExerciseSearchEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseSearchEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        SetEntryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class LiftLogDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun backupDao(): BackupDao
    abstract fun progressDao(): ProgressDao
    abstract fun workoutDao(): WorkoutDao
}
