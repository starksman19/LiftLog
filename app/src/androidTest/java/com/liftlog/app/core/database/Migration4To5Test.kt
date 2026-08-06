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

class Migration4To5Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-4-5-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = LiftLogDatabase::class,
    )

    @Test
    fun migrationAddsPlansAndKeepsExistingTemplatesUngrouped() = runBlocking {
        helper.createDatabase(4).use { connection ->
            connection.execSQL("INSERT INTO workout_templates (id, name, createdAtEpochMillis) VALUES (7, 'Push', 1)")
        }

        helper.runMigrationsAndValidate(5, listOf(DatabaseMigrations.Migration4To5)).use { connection ->
            connection.prepare("SELECT planId FROM workout_templates WHERE id = 7").use { statement ->
                assertTrue(statement.step())
                assertTrue(statement.isNull(0))
            }
            connection.execSQL("INSERT INTO workout_plans (name, createdAtEpochMillis) VALUES ('Plan A', 1)")
            connection.prepare("SELECT name FROM workout_plans").use { statement ->
                assertTrue(statement.step())
                assertEquals("Plan A", statement.getText(0))
            }
        }
    }
}
