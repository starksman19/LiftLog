package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.model.WorkoutTemplateRow
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Query(
        """
        SELECT t.id AS id, t.name AS name, COUNT(te.id) AS exerciseCount
        FROM workout_templates AS t
        LEFT JOIN workout_template_exercises AS te ON te.templateId = t.id
        GROUP BY t.id
        ORDER BY t.name COLLATE NOCASE
        """,
    )
    fun observeTemplates(): Flow<List<WorkoutTemplateRow>>

    @Query("SELECT exerciseId FROM workout_template_exercises WHERE templateId = :templateId ORDER BY orderIndex")
    suspend fun getExerciseIds(templateId: Long): List<Long>

    @Query("UPDATE workout_templates SET name = :name WHERE id = :templateId")
    suspend fun updateTemplateName(templateId: Long, name: String)

    @Query("DELETE FROM workout_template_exercises WHERE templateId = :templateId")
    suspend fun clearTemplateExercises(templateId: Long)

    @Query("DELETE FROM workout_templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTemplateExercises(exercises: List<WorkoutTemplateExerciseEntity>)

    @Query("SELECT id FROM workout_sessions WHERE finishedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis DESC LIMIT 1")
    suspend fun getActiveSessionId(): Long?

    @Query("SELECT exerciseId FROM workout_exercises WHERE workoutSessionId = :workoutSessionId ORDER BY orderIndex, id")
    suspend fun getWorkoutExerciseIds(workoutSessionId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)

    @Transaction
    suspend fun createTemplate(name: String, exerciseIds: List<Long>) {
        val templateId = insertTemplate(WorkoutTemplateEntity(name = name, createdAtEpochMillis = System.currentTimeMillis()))
        insertTemplateExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                WorkoutTemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, orderIndex = index)
            },
        )
    }

    @Transaction
    suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>) {
        updateTemplateName(templateId, name)
        clearTemplateExercises(templateId)
        insertTemplateExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                WorkoutTemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, orderIndex = index)
            },
        )
    }

    @Transaction
    suspend fun saveActiveWorkoutAsTemplate(name: String) {
        val sessionId = getActiveSessionId() ?: return
        val exerciseIds = getWorkoutExerciseIds(sessionId)
        if (exerciseIds.isNotEmpty()) createTemplate(name, exerciseIds)
    }

    @Transaction
    suspend fun startTemplate(templateId: Long, gymLocation: String?) {
        if (getActiveSessionId() != null) return
        val exerciseIds = getExerciseIds(templateId)
        if (exerciseIds.isEmpty()) return

        val sessionId = insertWorkoutSession(
            WorkoutSessionEntity(
                startedAtEpochMillis = System.currentTimeMillis(),
                gymLocation = gymLocation?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
        insertWorkoutExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                WorkoutExerciseEntity(
                    workoutSessionId = sessionId,
                    exerciseId = exerciseId,
                    orderIndex = index,
                )
            },
        )
    }
}
