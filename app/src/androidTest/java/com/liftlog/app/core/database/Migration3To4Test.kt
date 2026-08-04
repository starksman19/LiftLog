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

class Migration3To4Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-3-4-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = LiftLogDatabase::class,
    )

    @Test
    fun migrationCreatesLocationListFromExistingData() = runBlocking {
        helper.createDatabase(3).use { connection ->
            connection.execSQL(
                """
                INSERT INTO exercises (id, name, primaryMuscle, equipment, category, gymLocation, isCustom, createdAtEpochMillis)
                VALUES (22, 'Leg Press', 'Legs', 'Machine', 'Machine', 'North Gym', 1, 1)
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(4, listOf(DatabaseMigrations.Migration3To4)).use { connection ->
            connection.prepare("SELECT name FROM gym_locations WHERE name = 'North Gym'").use { statement ->
                assertTrue(statement.step())
                assertEquals("North Gym", statement.getText(0))
            }
        }
    }
}
