package com.liftlog.app.feature.timer.notification

import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.RestTimerMode

data class RestTimerNotificationState(
    val latestSetId: Long,
    val startedAtEpochMillis: Long,
    val offsetSeconds: Int,
    val bubbleEnabled: Boolean,
)

fun ActiveWorkout?.restTimerNotificationState(settings: AppSettings): RestTimerNotificationState? {
    if (!settings.restTimerNotificationsEnabled || settings.restTimerMode != RestTimerMode.Workout) {
        return null
    }

    val latestSet = this?.exercises
        ?.asSequence()
        ?.flatMap { it.sets.asSequence() }
        ?.filter { it.completedAtEpochMillis > 0 }
        ?.maxByOrNull { it.completedAtEpochMillis }
        ?: return null

    return RestTimerNotificationState(
        latestSetId = latestSet.id,
        startedAtEpochMillis = latestSet.completedAtEpochMillis,
        offsetSeconds = settings.restTimerOffsetSeconds,
        bubbleEnabled = settings.restTimerBubbleEnabled,
    )
}
