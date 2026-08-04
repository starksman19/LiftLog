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

class Migration2To3Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-2-3-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = LiftLogDatabase::class,
    )

    @Test
    fun migrationPreservesExistingExercises() = runBlocking {
        helper.createDatabase(2).use { connection ->
            connection.execSQL(
                "INSERT INTO exercises (id, name, primaryMuscle, equipment, isCustom, createdAtEpochMillis) VALUES (11, 'Bench Press', 'Chest', 'Barbell', 0, 1)",
            )
        }

        helper.runMigrationsAndValidate(3, listOf(DatabaseMigrations.Migration2To3)).use { connection ->
            connection.prepare("SELECT name, category FROM exercises WHERE id = 11").use { statement ->
                assertTrue(statement.step())
                assertEquals("Bench Press", statement.getText(0))
                assertEquals("FreeWeights", statement.getText(1))
            }
        }
    }
}
