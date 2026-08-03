package com.liftlog.app.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.liftlog.app.core.database.dao.ExerciseDao
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.ExerciseSearchEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseSearchEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LiftLogDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}

