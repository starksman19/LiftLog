package com.liftlog.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseImageStorageTest {
    @Test
    fun `single legacy image remains readable`() {
        val image = "data:image/jpeg;base64,AAAA"

        assertEquals(listOf(image), image.toExerciseImageUris())
    }

    @Test
    fun `multiple images round trip through existing storage field`() {
        val images = listOf(
            "data:image/jpeg;base64,AAAA",
            "data:image/jpeg;base64,BBBB",
        )

        assertEquals(images, images.toExerciseImageStorageValue().toExerciseImageUris())
    }
}
