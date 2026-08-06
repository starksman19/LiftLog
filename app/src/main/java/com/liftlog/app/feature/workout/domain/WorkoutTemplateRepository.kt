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
    suspend fun getTemplatePlanIds(templateId: Long): List<Long>
    suspend fun getPlanTemplateIds(planId: Long): List<Long>
    suspend fun createTemplate(name: String, exerciseIds: List<Long>, planIds: List<Long>)
    suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>, planIds: List<Long>)
    suspend fun deleteTemplate(templateId: Long)
    suspend fun createPlan(name: String, templateIds: List<Long>)
    suspend fun updatePlan(planId: Long, name: String, templateIds: List<Long>)
    suspend fun deletePlan(planId: Long)
}
