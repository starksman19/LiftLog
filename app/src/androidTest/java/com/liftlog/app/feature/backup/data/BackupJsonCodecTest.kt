package com.liftlog.app.feature.backup.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplatePlanEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.RestTimerMode
import com.liftlog.app.feature.backup.domain.BackupSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupJsonCodecTest {
    @Test
    fun roundTripPreservesRestTimerSettings() {
        val backup = LiftLogBackup(
            exportedAtEpochMillis = 1L,
            settings = AppSettings(restTimerMode = RestTimerMode.Exercise, restTimerOffsetSeconds = 18),
            snapshot = DatabaseSnapshot(
                exercises = emptyList(),
                workoutSessions = emptyList(),
                workoutExercises = emptyList(),
                setEntries = emptyList(),
                workoutTemplates = emptyList(),
                workoutTemplateExercises = emptyList(),
            ),
            selection = BackupSelection(
                settings = true,
                locations = false,
                exercises = false,
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
                workoutTemplates = false,
            ),
        )

        val restored = BackupJsonCodec.decode(BackupJsonCodec.encode(backup))

        assertEquals(RestTimerMode.Exercise, restored.settings?.restTimerMode)
        assertEquals(18, restored.settings?.restTimerOffsetSeconds)
    }

    @Test
    fun roundTripPreservesExercisePhotoAndCurrentExerciseFields() {
        val imageUri = "data:image/jpeg;base64,AAECAwQ="
        val selection = BackupSelection(
            settings = false,
            locations = false,
            exercises = true,
            workoutSessions = false,
            workoutExercises = false,
            setEntries = false,
            workoutTemplates = false,
        )
        val backup = LiftLogBackup(
            exportedAtEpochMillis = 1_000L,
            settings = null,
            selection = selection,
            snapshot = DatabaseSnapshot(
                exercises = listOf(
                    ExerciseEntity(
                        id = 10L,
                        name = "Maszyna na plecy",
                        primaryMuscle = "Plecy",
                        equipment = "Wioslowanie",
                        category = ExerciseCategory.Machine.name,
                        gymLocation = "Siłownia A",
                        youTubeUrl = "https://www.youtube.com/watch?v=example",
                        imageUri = imageUri,
                        isCustom = true,
                        createdAtEpochMillis = 999L,
                    ),
                ),
                workoutSessions = emptyList(),
                workoutExercises = emptyList(),
                setEntries = emptyList(),
                workoutTemplates = emptyList(),
                workoutTemplateExercises = emptyList(),
            ),
        )

        val restored = BackupJsonCodec.decode(BackupJsonCodec.encode(backup))

        assertEquals(imageUri, restored.snapshot.exercises.single().imageUri)
        assertEquals(ExerciseCategory.Machine.name, restored.snapshot.exercises.single().category)
        assertEquals("Siłownia A", restored.snapshot.exercises.single().gymLocation)
        assertEquals("https://www.youtube.com/watch?v=example", restored.snapshot.exercises.single().youTubeUrl)
    }

    @Test
    fun roundTripPreservesTimedExerciseCategory() {
        val backup = LiftLogBackup(
            exportedAtEpochMillis = 1L,
            settings = null,
            selection = BackupSelection(
                settings = false,
                locations = false,
                exercises = true,
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
                workoutTemplates = false,
            ),
            snapshot = DatabaseSnapshot(
                exercises = listOf(
                    ExerciseEntity(
                        id = 1L,
                        name = "Plank",
                        primaryMuscle = "Core",
                        equipment = "",
                        category = ExerciseCategory.Timed.name,
                        createdAtEpochMillis = 1L,
                    ),
                ),
                workoutSessions = emptyList(),
                workoutExercises = emptyList(),
                setEntries = emptyList(),
                workoutTemplates = emptyList(),
                workoutTemplateExercises = emptyList(),
            ),
        )

        val restored = BackupJsonCodec.decode(BackupJsonCodec.encode(backup))

        assertEquals(ExerciseCategory.Timed.name, restored.snapshot.exercises.single().category)
    }

    @Test
    fun backupKeepsTemplatesAndTheirExerciseReferences() {
        val exercise = ExerciseEntity(
            id = 7,
            name = "Leg Press",
            primaryMuscle = "Legs",
            equipment = "Machine",
            category = ExerciseCategory.Machine.name,
            gymLocation = "Main Gym",
            createdAtEpochMillis = 1,
        )
        val backup = LiftLogBackup(
            exportedAtEpochMillis = 42,
            settings = null,
            snapshot = DatabaseSnapshot(
                exercises = listOf(exercise),
                workoutSessions = emptyList(),
                workoutExercises = emptyList(),
                setEntries = emptyList(),
                workoutPlans = listOf(WorkoutPlanEntity(id = 9, name = "Upper body", createdAtEpochMillis = 2)),
                workoutTemplates = listOf(WorkoutTemplateEntity(id = 3, name = "Leg day", createdAtEpochMillis = 2)),
                workoutTemplateExercises = listOf(WorkoutTemplateExerciseEntity(id = 4, templateId = 3, exerciseId = 7, orderIndex = 0)),
                workoutTemplatePlans = listOf(WorkoutTemplatePlanEntity(templateId = 3, planId = 9)),
            ),
            selection = BackupSelection(
                settings = false,
                exercises = true,
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
                workoutTemplates = true,
            ),
        )

        val restored = BackupJsonCodec.decode(BackupJsonCodec.encode(backup))

        assertTrue(restored.selection.workoutTemplates)
        assertEquals("Leg day", restored.snapshot.workoutTemplates.single().name)
        assertEquals("Upper body", restored.snapshot.workoutPlans.single().name)
        assertEquals(9L, restored.snapshot.workoutTemplatePlans.single().planId)
        assertEquals(7L, restored.snapshot.workoutTemplateExercises.single().exerciseId)
    }

    @Test
    fun oldBackupWithoutCategoryDerivesAValidExerciseType() {
        val oldBackup = """
            {"formatVersion":1,"exportedAtEpochMillis":1,"settings":{"weightUnit":"Kilograms","defaultRestSeconds":60},"database":{"exercises":[{"id":1,"name":"Leg Press","primaryMuscle":"Legs","equipment":"Machine","isCustom":false,"createdAtEpochMillis":0}],"workoutSessions":[],"workoutExercises":[],"setEntries":[]}}
        """.trimIndent()

        val restored = BackupJsonCodec.decode(oldBackup)

        assertEquals(ExerciseCategory.Machine.name, restored.snapshot.exercises.single().category)
        assertTrue(restored.snapshot.workoutTemplates.isEmpty())
        assertEquals(60, restored.settings?.defaultRestSeconds)
        assertEquals(RestTimerMode.Workout, restored.settings?.restTimerMode)
        assertEquals(0, restored.settings?.restTimerOffsetSeconds)
    }

    @Test
    fun legacyTimerSwitchMapsToTheNewTimerMode() {
        val oldBackup = """
            {"formatVersion":7,"exportedAtEpochMillis":1,"sections":{"settings":true,"locations":false,"exercises":false,"workoutSessions":false,"workoutExercises":false,"setEntries":false,"workoutTemplates":false},"settings":{"weightUnit":"Kilograms","defaultRestSeconds":60,"restTimerEnabled":false,"restTimerOffsetSeconds":12},"database":{}}
        """.trimIndent()

        val restored = BackupJsonCodec.decode(oldBackup)

        assertEquals(RestTimerMode.Off, restored.settings?.restTimerMode)
        assertEquals(12, restored.settings?.restTimerOffsetSeconds)
    }
}
