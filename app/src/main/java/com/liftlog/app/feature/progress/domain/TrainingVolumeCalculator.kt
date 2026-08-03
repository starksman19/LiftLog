package com.liftlog.app.feature.progress.domain

object TrainingVolumeCalculator {
    fun calculate(weight: Double, reps: Int): Double {
        require(weight >= 0) { "Weight cannot be negative." }
        require(reps >= 0) { "Reps cannot be negative." }
        return weight * reps
    }
}

