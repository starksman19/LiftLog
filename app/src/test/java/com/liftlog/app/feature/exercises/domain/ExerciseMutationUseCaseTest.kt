package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.ExerciseDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseMutationUseCaseTest {
    @Test
    fun `updating a machine keeps its required gym location`() = runTest {
        val repository = FakeExerciseRepository()
        val draft = ExerciseDraft("Leg Press", "Legs", "Machine", ExerciseCategory.Machine, "Main Gym", null, null)

        UpdateExerciseUseCase(repository)(12, draft)

        assertEquals(12L, repository.updatedId)
        assertEquals("Main Gym", repository.updatedDraft?.gymLocation)
    }

    @Test
    fun `deleting an exercise delegates its id`() = runTest {
        val repository = FakeExerciseRepository()

        DeleteExerciseUseCase(repository)(21)

        assertEquals(21L, repository.deletedId)
    }

    private class FakeExerciseRepository : ExerciseRepository {
        var updatedId: Long? = null
        var updatedDraft: ExerciseDraft? = null
        var deletedId: Long? = null

        override fun observeExercises(query: String): Flow<List<Exercise>> = emptyFlow()
        override suspend fun ensureStarterExercises() = Unit
        override suspend fun addCustomExercise(draft: ExerciseDraft) = Unit
        override suspend fun updateExercise(exerciseId: Long, draft: ExerciseDraft) {
            updatedId = exerciseId
            updatedDraft = draft
        }
        override suspend fun deleteExercise(exerciseId: Long) {
            deletedId = exerciseId
        }
    }
}
