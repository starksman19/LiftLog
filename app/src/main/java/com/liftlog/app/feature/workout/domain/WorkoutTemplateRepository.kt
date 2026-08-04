package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

interface WorkoutTemplateRepository {
    fun observeTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun saveActiveWorkoutAsTemplate(name: String)
    suspend fun startTemplate(templateId: Long, gymLocation: String?)
    suspend fun getTemplateExerciseIds(templateId: Long): List<Long>
    suspend fun createTemplate(name: String, exerciseIds: List<Long>)
    suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>)
    suspend fun deleteTemplate(templateId: Long)
}
