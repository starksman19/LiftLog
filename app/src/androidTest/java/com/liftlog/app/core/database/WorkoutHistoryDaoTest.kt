package com.liftlog.app.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.model.ExerciseCategory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WorkoutHistoryDaoTest {
    private lateinit var database: LiftLogDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            LiftLogDatabase::class.java,
        )
            .setDriver(BundledSQLiteDriver())
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exerciseHistoryContainsAllCompletedWorkouts() = runBlocking {
        val exerciseId = database.exerciseDao().insertExercise(
            ExerciseEntity(
                name = "Bench Press",
                primaryMuscle = "Chest",
                equipment = "Barbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 1L,
            ),
        )
        listOf(1_000L, 2_000L, 3_000L).forEach { finishedAt ->
            val sessionId = database.workoutDao().insertWorkoutSession(
                WorkoutSessionEntity(startedAtEpochMillis = finishedAt - 100L, finishedAtEpochMillis = finishedAt),
            )
            val workoutExerciseId = database.workoutDao().insertWorkoutExercise(
                WorkoutExerciseEntity(workoutSessionId = sessionId, exerciseId = exerciseId, orderIndex = 0),
            )
            database.workoutDao().insertSetEntry(
                SetEntryEntity(workoutExerciseId = workoutExerciseId, setNumber = 1, weight = 70.0, reps = 8, completedAtEpochMillis = finishedAt),
            )
        }

        val history = database.workoutDao().getRecentExercisePerformances(exerciseId)

        assertEquals(3, history.size)
        assertEquals(listOf(3_000L, 2_000L, 1_000L), history.map { it.finishedAtEpochMillis })
    }
}
