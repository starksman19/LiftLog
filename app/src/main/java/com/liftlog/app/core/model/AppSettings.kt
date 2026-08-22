package com.liftlog.app.core.model

data class AppSettings(
    val weightUnit: WeightUnit = WeightUnit.Kilograms,
    val defaultRestSeconds: Int = 90,
    val restTimerMode: RestTimerMode = RestTimerMode.Workout,
    val restTimerOffsetSeconds: Int = 0,
    val restTimerNotificationsEnabled: Boolean = false,
    val restTimerBubbleEnabled: Boolean = true,
)

enum class RestTimerMode {
    Workout,
    Exercise,
    Off,
}
