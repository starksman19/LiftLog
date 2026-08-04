package com.liftlog.app.core.database

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object DatabaseMigrations {
    val Migration2To3 = object : Migration(2, 3) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE exercises ADD COLUMN category TEXT NOT NULL DEFAULT 'FreeWeights'")
            connection.execSQL("ALTER TABLE exercises ADD COLUMN gymLocation TEXT")
            connection.execSQL("ALTER TABLE exercises ADD COLUMN youTubeUrl TEXT")
            connection.execSQL("ALTER TABLE exercises ADD COLUMN imageUri TEXT")
            connection.execSQL("ALTER TABLE workout_sessions ADD COLUMN gymLocation TEXT")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS workout_templates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAtEpochMillis INTEGER NOT NULL)",
            )
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS workout_template_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, templateId INTEGER NOT NULL, exerciseId INTEGER NOT NULL, orderIndex INTEGER NOT NULL, FOREIGN KEY(templateId) REFERENCES workout_templates(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_workout_template_exercises_templateId_orderIndex ON workout_template_exercises(templateId, orderIndex)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_workout_template_exercises_exerciseId ON workout_template_exercises(exerciseId)")
        }
    }
}
