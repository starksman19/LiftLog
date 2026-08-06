package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.core.model.WorkoutPlan
import com.liftlog.app.feature.workout.domain.ObserveWorkoutPlansUseCase
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.workout.domain.ObserveWorkoutTemplatesUseCase
import com.liftlog.app.feature.workout.domain.WorkoutTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TemplateManagementViewModel @Inject constructor(
    observeWorkoutTemplatesUseCase: ObserveWorkoutTemplatesUseCase,
    observeWorkoutPlansUseCase: ObserveWorkoutPlansUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    private val repository: WorkoutTemplateRepository,
) : ViewModel() {
    val uiState: StateFlow<TemplateManagementUiState> = combine(
        observeWorkoutTemplatesUseCase(),
        observeWorkoutPlansUseCase(),
        observeExercisesUseCase(""),
    ) { templates, plans, exercises -> TemplateManagementUiState(templates, plans, exercises) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TemplateManagementUiState())

    fun loadExerciseIds(templateId: Long, onLoaded: (Set<Long>) -> Unit) {
        viewModelScope.launch { onLoaded(repository.getTemplateExerciseIds(templateId).toSet()) }
    }

    fun loadTemplatePlanIds(templateId: Long, onLoaded: (Set<Long>) -> Unit) {
        viewModelScope.launch { onLoaded(repository.getTemplatePlanIds(templateId).toSet()) }
    }

    fun loadPlanTemplateIds(planId: Long, onLoaded: (Set<Long>) -> Unit) {
        viewModelScope.launch { onLoaded(repository.getPlanTemplateIds(planId).toSet()) }
    }

    fun save(templateId: Long?, name: String, exerciseIds: Set<Long>, planIds: Set<Long>) {
        if (name.isBlank() || exerciseIds.isEmpty()) return
        viewModelScope.launch {
            if (templateId == null) repository.createTemplate(name.trim(), exerciseIds.toList(), planIds.toList())
            else repository.updateTemplate(templateId, name.trim(), exerciseIds.toList(), planIds.toList())
        }
    }

    fun delete(templateId: Long) {
        viewModelScope.launch { repository.deleteTemplate(templateId) }
    }

    fun savePlan(planId: Long?, name: String, templateIds: Set<Long>) {
        if (name.isBlank()) return
        viewModelScope.launch {
            if (planId == null) repository.createPlan(name.trim(), templateIds.toList())
            else repository.updatePlan(planId, name.trim(), templateIds.toList())
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch { repository.deletePlan(planId) }
    }
}

data class TemplateManagementUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val plans: List<WorkoutPlan> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
)
