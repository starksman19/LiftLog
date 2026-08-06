package com.liftlog.app.feature.backup.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.feature.backup.domain.BackupSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupJsonCodecTest {
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
                workoutTemplates = listOf(WorkoutTemplateEntity(id = 3, name = "Leg day", createdAtEpochMillis = 2, planId = 9)),
                workoutTemplateExercises = listOf(WorkoutTemplateExerciseEntity(id = 4, templateId = 3, exerciseId = 7, orderIndex = 0)),
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
        assertEquals(9L, restored.snapshot.workoutTemplates.single().planId)
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
    }
}
