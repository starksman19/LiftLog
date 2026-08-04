package com.liftlog.app.feature.workout.data

import com.liftlog.app.core.database.dao.WorkoutTemplateDao
import com.liftlog.app.core.database.model.WorkoutTemplateRow
import com.liftlog.app.core.model.WorkoutTemplate
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

    override suspend fun saveActiveWorkoutAsTemplate(name: String) {
        templateDao.saveActiveWorkoutAsTemplate(name)
    }

    override suspend fun startTemplate(templateId: Long, gymLocation: String?) {
        templateDao.startTemplate(templateId, gymLocation)
    }

    override suspend fun getTemplateExerciseIds(templateId: Long): List<Long> = templateDao.getExerciseIds(templateId)

    override suspend fun createTemplate(name: String, exerciseIds: List<Long>) {
        templateDao.createTemplate(name, exerciseIds)
    }

    override suspend fun updateTemplate(templateId: Long, name: String, exerciseIds: List<Long>) {
        templateDao.updateTemplate(templateId, name, exerciseIds)
    }

    override suspend fun deleteTemplate(templateId: Long) {
        templateDao.deleteTemplate(templateId)
    }

    private fun WorkoutTemplateRow.toModel() = WorkoutTemplate(
        id = id,
        name = name,
        exerciseCount = exerciseCount,
    )
}
