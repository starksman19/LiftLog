package com.liftlog.app.feature.timer.notification

import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.LoggedExercise
import com.liftlog.app.core.model.LoggedSet
import com.liftlog.app.core.model.RestTimerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RestTimerNotificationStateTest {
    @Test
    fun createsStateForTheNewestSetWhenWorkoutTimerNotificationsAreEnabled() {
        val state = activeWorkout().restTimerNotificationState(
            AppSettings(
                restTimerMode = RestTimerMode.Workout,
                restTimerNotificationsEnabled = true,
                restTimerBubbleEnabled = false,
                restTimerOffsetSeconds = 12,
            ),
        )

        requireNotNull(state)
        assertEquals(2L, state.latestSetId)
        assertEquals(2_000L, state.startedAtEpochMillis)
        assertEquals(12, state.offsetSeconds)
        assertEquals(false, state.bubbleEnabled)
    }

    @Test
    fun doesNotCreateStateForExerciseTimersOrDisabledNotifications() {
        assertNull(
            activeWorkout().restTimerNotificationState(
                AppSettings(restTimerMode = RestTimerMode.Exercise, restTimerNotificationsEnabled = true),
            ),
        )
        assertNull(
            activeWorkout().restTimerNotificationState(
                AppSettings(restTimerMode = RestTimerMode.Workout, restTimerNotificationsEnabled = false),
            ),
        )
    }

    private fun activeWorkout() = ActiveWorkout(
        id = 1,
        startedAtEpochMillis = 0,
        gymLocation = null,
        notes = null,
        exercises = listOf(
            LoggedExercise(
                id = 1,
                exerciseId = 1,
                name = "Bench press",
                category = com.liftlog.app.core.model.ExerciseCategory.FreeWeights,
                primaryMuscle = "Chest",
                equipment = "Barbell",
                orderIndex = 0,
                notes = null,
                sets = listOf(
                    LoggedSet(id = 1, setNumber = 1, weight = 50.0, reps = 8, completedAtEpochMillis = 1_000L),
                    LoggedSet(id = 2, setNumber = 2, weight = 50.0, reps = 8, completedAtEpochMillis = 2_000L),
                ),
            ),
        ),
    )
}
