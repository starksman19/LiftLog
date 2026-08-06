package com.liftlog.app.feature.backup.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.liftlog.app.core.database.LiftLogDatabase
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplatePlanEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot
import com.liftlog.app.core.model.ExerciseCategory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupDaoImportTest {
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
    fun importingTemplatesAlsoImportsTheirExercisesWithoutReplacingWorkouts() = runBlocking {
        val backupDao = database.backupDao()
        backupDao.insertWorkoutSessions(
            listOf(WorkoutSessionEntity(id = 50L, startedAtEpochMillis = 1L, finishedAtEpochMillis = 2L)),
        )

        backupDao.mergeExercisesAndReplaceWorkouts(
            snapshot = DatabaseSnapshot(
                exercises = listOf(
                    ExerciseEntity(
                        id = 10L,
                        name = "Przysiad",
                        primaryMuscle = "Nogi",
                        equipment = "Sztanga",
                        category = ExerciseCategory.FreeWeights.name,
                        createdAtEpochMillis = 1L,
                    ),
                ),
                workoutSessions = emptyList(),
                workoutExercises = emptyList(),
                setEntries = emptyList(),
                workoutPlans = listOf(WorkoutPlanEntity(id = 40L, name = "Plan A", createdAtEpochMillis = 2L)),
                workoutTemplates = listOf(WorkoutTemplateEntity(id = 20L, name = "Nogi", createdAtEpochMillis = 2L)),
                workoutTemplateExercises = listOf(
                    WorkoutTemplateExerciseEntity(id = 30L, templateId = 20L, exerciseId = 10L, orderIndex = 0),
                ),
                workoutTemplatePlans = listOf(WorkoutTemplatePlanEntity(templateId = 20L, planId = 40L)),
            ),
            replaceWorkoutData = false,
        )

        assertEquals(1, backupDao.getWorkoutSessions().size)
        assertEquals("Nogi", backupDao.getWorkoutTemplates().single().name)
        assertEquals(1, backupDao.getWorkoutTemplateExercises().size)
        assertEquals("Plan A", backupDao.getWorkoutPlans().single().name)
        assertEquals(1, backupDao.getWorkoutTemplatePlans().size)
    }
}
