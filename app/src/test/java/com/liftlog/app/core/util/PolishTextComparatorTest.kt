package com.liftlog.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PolishTextComparatorTest {
    @Test
    fun `orders names using the Polish alphabet`() {
        val names = listOf("Żaba", "Zebra", "Ąda", "Adam", "Ćma", "Cezar")

        assertEquals(
            listOf("Adam", "Ąda", "Cezar", "Ćma", "Zebra", "Żaba"),
            names.sortedWith(PolishTextComparator),
        )
    }
}
