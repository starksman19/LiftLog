package com.liftlog.app.feature.progress.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingVolumeCalculatorTest {
    @Test
    fun calculate_returnsWeightMultipliedByReps() {
        val volume = TrainingVolumeCalculator.calculate(weight = 100.0, reps = 5)

        assertEquals(500.0, volume, 0.0)
    }
}

