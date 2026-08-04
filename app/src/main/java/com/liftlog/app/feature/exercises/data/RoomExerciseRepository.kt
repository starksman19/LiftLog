package com.liftlog.app.feature.exercises.data

import com.liftlog.app.core.database.dao.ExerciseDao
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.toModel
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.ExerciseDraft
import com.liftlog.app.feature.exercises.domain.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {
    override fun observeExercises(query: String): Flow<List<Exercise>> {
        val ftsQuery = query.toFtsQuery()
        val source = if (ftsQuery == null) {
            exerciseDao.observeExercises()
        } else {
            exerciseDao.searchExercises(ftsQuery)
        }

        return source.map { exercises ->
            exercises.map { it.toModel() }
        }
    }

    override suspend fun ensureStarterExercises() {
        if (exerciseDao.countExercises() > 0) return

        starterExercises.forEach { exercise ->
            exerciseDao.insertExerciseWithSearch(exercise)
        }
    }

    override suspend fun addCustomExercise(draft: ExerciseDraft) {
        exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(
                name = draft.name,
                primaryMuscle = draft.primaryMuscle,
                equipment = draft.equipment,
                category = draft.category.name,
                gymLocation = draft.gymLocation,
                youTubeUrl = draft.youTubeUrl,
                imageUri = draft.imageUri,
                isCustom = true,
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun updateExercise(exerciseId: Long, draft: ExerciseDraft) {
        exerciseDao.updateExerciseWithSearch(
            ExerciseEntity(
                id = exerciseId,
                name = draft.name,
                primaryMuscle = draft.primaryMuscle,
                equipment = draft.equipment,
                category = draft.category.name,
                gymLocation = draft.gymLocation,
                youTubeUrl = draft.youTubeUrl,
                imageUri = draft.imageUri,
                isCustom = true,
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun deleteExercise(exerciseId: Long) {
        exerciseDao.deleteExercise(exerciseId)
    }

    private fun String.toFtsQuery(): String? {
        val tokens = trim()
            .split(Regex("\\s+"))
            .map { token -> token.replace(Regex("[^\\p{L}\\p{Nd}_]"), "") }
            .filter { it.isNotBlank() }

        return tokens
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = " ") { token -> "$token*" }
    }

    private companion object {
        val starterExercises = listOf(
            ExerciseEntity(
                name = "Bench Press",
                primaryMuscle = "Chest",
                equipment = "Barbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Squat",
                primaryMuscle = "Legs",
                equipment = "Barbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Deadlift",
                primaryMuscle = "Back",
                equipment = "Barbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Overhead Press",
                primaryMuscle = "Shoulders",
                equipment = "Barbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Lat Pulldown",
                primaryMuscle = "Back",
                equipment = "Machine",
                category = ExerciseCategory.Machine.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Dumbbell Row",
                primaryMuscle = "Back",
                equipment = "Dumbbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Leg Press",
                primaryMuscle = "Legs",
                equipment = "Machine",
                category = ExerciseCategory.Machine.name,
                createdAtEpochMillis = 0,
            ),
            ExerciseEntity(
                name = "Biceps Curl",
                primaryMuscle = "Arms",
                equipment = "Dumbbell",
                category = ExerciseCategory.FreeWeights.name,
                createdAtEpochMillis = 0,
            ),
        )
    }
}
