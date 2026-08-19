package com.liftlog.app.core.model

data class AppSettings(
    val weightUnit: WeightUnit = WeightUnit.Kilograms,
    val defaultRestSeconds: Int = 90,
    val restTimerEnabled: Boolean = true,
    val restTimerOffsetSeconds: Int = 0,
)
