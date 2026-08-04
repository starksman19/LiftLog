package com.liftlog.app.feature.backup.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSelectionTest {
    @Test
    fun `selecting a set includes the complete workout package`() {
        val selection = BackupSelection(
            settings = false,
            exercises = false,
            workoutSessions = false,
            workoutExercises = false,
            setEntries = false,
        ).toggled(BackupSection.SetEntries, true)

        assertTrue(selection.exercises)
        assertTrue(selection.workoutSessions)
        assertTrue(selection.workoutExercises)
        assertTrue(selection.setEntries)
    }

    @Test
    fun `removing exercises removes dependent workout data`() {
        val selection = BackupSelection.Everything.toggled(BackupSection.Exercises, false)

        assertFalse(selection.exercises)
        assertFalse(selection.workoutSessions)
        assertFalse(selection.workoutExercises)
        assertFalse(selection.setEntries)
        assertFalse(selection.workoutTemplates)
        assertTrue(selection.settings)
    }

    @Test
    fun `selecting a template includes exercises`() {
        val selection = BackupSelection(
            settings = false,
            exercises = false,
            workoutSessions = false,
            workoutExercises = false,
            setEntries = false,
            workoutTemplates = false,
        ).toggled(BackupSection.WorkoutTemplates, true)

        assertTrue(selection.exercises)
        assertTrue(selection.workoutTemplates)
        assertFalse(selection.hasWorkoutData())
    }
}
