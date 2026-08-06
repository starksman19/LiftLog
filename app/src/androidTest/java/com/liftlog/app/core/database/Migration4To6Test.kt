package com.liftlog.app.core.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class Migration4To6Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-4-6-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = LiftLogDatabase::class,
    )

    @Test
    fun migrationFromMainVersionKeepsTemplatesAndExerciseLinks() = runBlocking {
        helper.createDatabase(4).use { connection ->
            connection.execSQL(
                "INSERT INTO exercises (id, name, primaryMuscle, equipment, category, isCustom, createdAtEpochMillis) VALUES (11, 'Bench Press', 'Chest', 'Barbell', 'FreeWeights', 1, 1)",
            )
            connection.execSQL("INSERT INTO workout_templates (id, name, createdAtEpochMillis) VALUES (7, 'Push', 1)")
            connection.execSQL("INSERT INTO workout_template_exercises (id, templateId, exerciseId, orderIndex) VALUES (8, 7, 11, 0)")
        }

        helper.runMigrationsAndValidate(
            6,
            listOf(DatabaseMigrations.Migration4To5, DatabaseMigrations.Migration5To6),
        ).use { connection ->
            connection.prepare("SELECT name FROM workout_templates WHERE id = 7").use { statement ->
                assertTrue(statement.step())
                assertEquals("Push", statement.getText(0))
            }
            connection.prepare("SELECT exerciseId FROM workout_template_exercises WHERE templateId = 7").use { statement ->
                assertTrue(statement.step())
                assertEquals(11L, statement.getLong(0))
            }
            connection.prepare("SELECT COUNT(*) FROM workout_template_plans WHERE templateId = 7").use { statement ->
                assertTrue(statement.step())
                assertEquals(0L, statement.getLong(0))
            }
        }
    }
}
