package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTemplateExercises(exercises: List<WorkoutTemplateExerciseEntity>)

    @Transaction
    suspend fun createTemplate(name: String, exerciseIds: List<Long>) {
        val templateId = insertTemplate(WorkoutTemplateEntity(name = name, createdAtEpochMillis = System.currentTimeMillis()))
        insertTemplateExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                WorkoutTemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, orderIndex = index)
            },
        )
    }
}
