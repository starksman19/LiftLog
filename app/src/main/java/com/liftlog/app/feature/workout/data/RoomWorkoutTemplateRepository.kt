package com.liftlog.app.feature.workout.data

import com.liftlog.app.core.database.dao.WorkoutTemplateDao
import com.liftlog.app.core.database.model.WorkoutTemplateRow
import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.core.model.WorkoutPlan
import com.liftlog.app.feature.workout.domain.WorkoutTemplateRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomWorkoutTemplateRepository @Inject constructor(
    private val templateDao: WorkoutTemplateDao,
) : WorkoutTemplateRepository {
    override fun observeTemplates(): Flow<List<WorkoutTemplate>> = templateDao.observeTemplates()
        .map { rows -> rows.map { row -> row.toModel() } }

    override fun observePlans(): Flow<List<WorkoutPlan>> = templateDao.observePlans()
        .map { plans -> plans.map { plan -> WorkoutPlan(plan.id, plan.name) } }

    override suspend fun saveActiveWorkoutAsTemplate(name: String) {
        templateDao.saveActiveWorkoutAsTemplate(name)
    }

    override suspend fun startTemplate(templateId: Long, gymLocation: String?) {
        templateDao.startTemplate(templateId, gymLocation)
    }

    override suspend fun getTemplateExerciseIds(templateId: Long): List<Long> = templateDao.getExerciseIds(templateId)

    override suspend fun createTemplate(name: String, exerciseIds: List<Long>, planId: Long?) {
        templateDao.createTemplate(name, exerciseIds, planId)
    }

    override suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>, planId: Long?) {
        templateDao.updateTemplate(templateId, name, exerciseIds, planId)
    }

    override suspend fun deleteTemplate(templateId: Long) {
        templateDao.deleteTemplate(templateId)
    }

    override suspend fun createPlan(name: String) {
        templateDao.insertPlan(com.liftlog.app.core.database.entity.WorkoutPlanEntity(name = name, createdAtEpochMillis = System.currentTimeMillis()))
    }

    override suspend fun updatePlan(planId: Long, name: String) {
        templateDao.updatePlanName(planId, name)
    }

    override suspend fun deletePlan(planId: Long) {
        templateDao.deletePlan(planId)
    }

    private fun WorkoutTemplateRow.toModel() = WorkoutTemplate(
        id = id,
        name = name,
        exerciseCount = exerciseCount,
        planId = planId,
        planName = planName,
    )
}
