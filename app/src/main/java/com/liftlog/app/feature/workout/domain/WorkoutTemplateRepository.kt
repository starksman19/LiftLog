package com.liftlog.app.feature.workout.domain

import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.core.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface WorkoutTemplateRepository {
    fun observeTemplates(): Flow<List<WorkoutTemplate>>
    fun observePlans(): Flow<List<WorkoutPlan>>
    suspend fun saveActiveWorkoutAsTemplate(name: String)
    suspend fun startTemplate(templateId: Long, gymLocation: String?)
    suspend fun getTemplateExerciseIds(templateId: Long): List<Long>
    suspend fun createTemplate(name: String, exerciseIds: List<Long>, planId: Long?)
    suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>, planId: Long?)
    suspend fun deleteTemplate(templateId: Long)
    suspend fun createPlan(name: String)
    suspend fun updatePlan(planId: Long, name: String)
    suspend fun deletePlan(planId: Long)
}
