package com.liftlog.app.feature.backup.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.liftlog.app.core.database.LiftLogDatabase
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.GymLocationEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplatePlanEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.core.model.RestTimerMode
import com.liftlog.app.feature.backup.domain.BackupSelection
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

    @Test
    fun fullBackupRoundTripImportsEveryCurrentDataSection() = runBlocking {
        val backupDao = database.backupDao()
        val backup = LiftLogBackup(
            exportedAtEpochMillis = 1_000L,
            settings = AppSettings(
                weightUnit = WeightUnit.Pounds,
                defaultRestSeconds = 120,
                restTimerMode = RestTimerMode.Off,
                restTimerOffsetSeconds = 15,
            ),
            selection = BackupSelection.Everything,
            snapshot = DatabaseSnapshot(
                gymLocations = listOf(GymLocationEntity(id = 1L, name = "Home gym", createdAtEpochMillis = 1L)),
                exercises = listOf(
                    ExerciseEntity(
                        id = 10L,
                        name = "Leg press",
                        primaryMuscle = "Legs",
                        equipment = "Plate loaded machine",
                        category = ExerciseCategory.Machine.name,
                        youTubeUrl = "https://youtube.com/watch?v=leg-press",
                        imageUri = "data:image/jpeg;base64,AAECAwQ=",
                        isCustom = true,
                        createdAtEpochMillis = 2L,
                    ),
                ),
                workoutSessions = listOf(
                    WorkoutSessionEntity(
                        id = 20L,
                        startedAtEpochMillis = 3L,
                        finishedAtEpochMillis = 4L,
                        gymLocation = "Home gym",
                        notes = "Heavy day",
                    ),
                ),
                workoutExercises = listOf(
                    WorkoutExerciseEntity(
                        id = 30L,
                        workoutSessionId = 20L,
                        exerciseId = 10L,
                        orderIndex = 0,
                        notes = "Controlled tempo",
                    ),
                ),
                setEntries = listOf(
                    SetEntryEntity(
                        id = 40L,
                        workoutExerciseId = 30L,
                        setNumber = 1,
                        weight = 180.0,
                        reps = 10,
                        rpe = 8.5,
                        restSeconds = 120,
                        notes = "Good form",
                        completedAtEpochMillis = 5L,
                    ),
                ),
                workoutPlans = listOf(WorkoutPlanEntity(id = 50L, name = "Lower", createdAtEpochMillis = 6L)),
                workoutTemplates = listOf(WorkoutTemplateEntity(id = 60L, name = "Leg day", createdAtEpochMillis = 7L)),
                workoutTemplateExercises = listOf(
                    WorkoutTemplateExerciseEntity(id = 70L, templateId = 60L, exerciseId = 10L, orderIndex = 0),
                ),
                workoutTemplatePlans = listOf(WorkoutTemplatePlanEntity(templateId = 60L, planId = 50L)),
            ),
        )

        val decoded = BackupJsonCodec.decode(BackupJsonCodec.encode(backup))
        backupDao.mergeExercisesAndReplaceWorkouts(decoded.snapshot, decoded.selection.hasWorkoutData())
        val imported = backupDao.snapshot()

        assertEquals(WeightUnit.Pounds, decoded.settings?.weightUnit)
        assertEquals(RestTimerMode.Off, decoded.settings?.restTimerMode)
        assertEquals(15, decoded.settings?.restTimerOffsetSeconds)
        assertEquals("Home gym", imported.gymLocations.single().name)
        assertEquals("https://youtube.com/watch?v=leg-press", imported.exercises.single().youTubeUrl)
        assertEquals("data:image/jpeg;base64,AAECAwQ=", imported.exercises.single().imageUri)
        assertEquals("Heavy day", imported.workoutSessions.single().notes)
        assertEquals("Home gym", imported.workoutSessions.single().gymLocation)
        assertEquals("Controlled tempo", imported.workoutExercises.single().notes)
        assertEquals(imported.exercises.single().id, imported.workoutExercises.single().exerciseId)
        assertEquals(180.0, imported.setEntries.single().weight, 0.0)
        assertEquals(8.5, imported.setEntries.single().rpe ?: 0.0, 0.0)
        assertEquals(120, imported.setEntries.single().restSeconds)
        assertEquals("Good form", imported.setEntries.single().notes)
        assertEquals("Lower", imported.workoutPlans.single().name)
        assertEquals("Leg day", imported.workoutTemplates.single().name)
        assertEquals(1, imported.workoutTemplateExercises.size)
        assertEquals(1, imported.workoutTemplatePlans.size)
    }
}
