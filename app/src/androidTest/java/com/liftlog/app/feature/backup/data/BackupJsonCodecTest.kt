package com.liftlog.app.feature.backup.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
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
                workoutTemplates = listOf(WorkoutTemplateEntity(id = 3, name = "Leg day", createdAtEpochMillis = 2)),
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
