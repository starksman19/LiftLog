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

class Migration5To6Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-5-6-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = LiftLogDatabase::class,
    )

    @Test
    fun migrationMovesPreviousPlanAssignmentToLinkTable() = runBlocking {
        helper.createDatabase(5).use { connection ->
            connection.execSQL("INSERT INTO workout_plans (id, name, createdAtEpochMillis) VALUES (3, 'Push', 1)")
            connection.execSQL("INSERT INTO workout_templates (id, name, createdAtEpochMillis, planId) VALUES (7, 'Bench', 1, 3)")
        }

        helper.runMigrationsAndValidate(6, listOf(DatabaseMigrations.Migration5To6)).use { connection ->
            connection.prepare("SELECT planId FROM workout_template_plans WHERE templateId = 7").use { statement ->
                assertTrue(statement.step())
                assertEquals(3L, statement.getLong(0))
            }
        }
    }
}
