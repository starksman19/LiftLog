package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplatePlanEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.model.WorkoutTemplateRow
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Query(
        """
        SELECT t.id AS id, t.name AS name, COUNT(DISTINCT te.id) AS exerciseCount,
               GROUP_CONCAT(DISTINCT tp.planId) AS planIds,
               GROUP_CONCAT(DISTINCT p.name) AS planNames
        FROM workout_templates AS t
        LEFT JOIN workout_template_exercises AS te ON te.templateId = t.id
        LEFT JOIN workout_template_plans AS tp ON tp.templateId = t.id
        LEFT JOIN workout_plans AS p ON p.id = tp.planId
        GROUP BY t.id
        ORDER BY t.name COLLATE NOCASE
        """,
    )
    fun observeTemplates(): Flow<List<WorkoutTemplateRow>>

    @Query("SELECT * FROM workout_plans ORDER BY name COLLATE NOCASE")
    fun observePlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT exerciseId FROM workout_template_exercises WHERE templateId = :templateId ORDER BY orderIndex")
    suspend fun getExerciseIds(templateId: Long): List<Long>

    @Query("SELECT planId FROM workout_template_plans WHERE templateId = :templateId ORDER BY planId")
    suspend fun getPlanIds(templateId: Long): List<Long>

    @Query("SELECT templateId FROM workout_template_plans WHERE planId = :planId ORDER BY templateId")
    suspend fun getPlanTemplateIds(planId: Long): List<Long>

    @Query("UPDATE workout_templates SET name = :name WHERE id = :templateId")
    suspend fun updateTemplateName(templateId: Long, name: String)

    @Query("DELETE FROM workout_template_exercises WHERE templateId = :templateId")
    suspend fun clearTemplateExercises(templateId: Long)

    @Query("DELETE FROM workout_template_plans WHERE templateId = :templateId")
    suspend fun clearTemplatePlans(templateId: Long)

    @Query("DELETE FROM workout_templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Query("UPDATE workout_plans SET name = :name WHERE id = :planId")
    suspend fun updatePlanName(planId: Long, name: String)

    @Query("DELETE FROM workout_template_plans WHERE planId = :planId")
    suspend fun clearPlanTemplates(planId: Long)

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deletePlanRecord(planId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTemplateExercises(exercises: List<WorkoutTemplateExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplatePlans(plans: List<WorkoutTemplatePlanEntity>)

    @Query("SELECT id FROM workout_sessions WHERE finishedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis DESC LIMIT 1")
    suspend fun getActiveSessionId(): Long?

    @Query("SELECT exerciseId FROM workout_exercises WHERE workoutSessionId = :workoutSessionId ORDER BY orderIndex, id")
    suspend fun getWorkoutExerciseIds(workoutSessionId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)

    @Transaction
    suspend fun createTemplate(name: String, exerciseIds: List<Long>, planIds: List<Long> = emptyList()) {
        val templateId = insertTemplate(WorkoutTemplateEntity(name = name, createdAtEpochMillis = System.currentTimeMillis()))
        insertTemplateExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                WorkoutTemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, orderIndex = index)
            },
        )
        insertTemplatePlans(planIds.distinct().map { planId -> WorkoutTemplatePlanEntity(templateId, planId) })
    }

    @Transaction
    suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>, planIds: List<Long>? = null) {
        updateTemplateName(templateId, name)
        clearTemplateExercises(templateId)
        insertTemplateExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                WorkoutTemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, orderIndex = index)
            },
        )
        if (planIds != null) {
            clearTemplatePlans(templateId)
            insertTemplatePlans(planIds.distinct().map { planId -> WorkoutTemplatePlanEntity(templateId, planId) })
        }
    }

    @Transaction
    suspend fun deletePlan(planId: Long) {
        clearPlanTemplates(planId)
        deletePlanRecord(planId)
    }

    @Transaction
    suspend fun createPlan(name: String, templateIds: List<Long>) {
        val planId = insertPlan(WorkoutPlanEntity(name = name, createdAtEpochMillis = System.currentTimeMillis()))
        insertTemplatePlans(templateIds.distinct().map { templateId -> WorkoutTemplatePlanEntity(templateId, planId) })
    }

    @Transaction
    suspend fun updatePlan(planId: Long, name: String, templateIds: List<Long>) {
        updatePlanName(planId, name)
        clearPlanTemplates(planId)
        insertTemplatePlans(templateIds.distinct().map { templateId -> WorkoutTemplatePlanEntity(templateId, planId) })
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
