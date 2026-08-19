package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseDraft
import com.liftlog.app.core.model.RecentExercisePerformance
import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.core.model.WorkoutPlan
import com.liftlog.app.feature.exercises.domain.AddCustomExerciseUseCase
import com.liftlog.app.feature.exercises.domain.EnsureStarterExercisesUseCase
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.locations.domain.GymLocationRepository
import com.liftlog.app.feature.workout.domain.AddExerciseToActiveWorkoutUseCase
import com.liftlog.app.feature.workout.domain.AddSetUseCase
import com.liftlog.app.feature.workout.domain.DiscardWorkoutUseCase
import com.liftlog.app.feature.workout.domain.DeleteSetUseCase
import com.liftlog.app.feature.workout.domain.FinishWorkoutUseCase
import com.liftlog.app.feature.workout.domain.GetRecentExercisePerformancesUseCase
import com.liftlog.app.feature.workout.domain.ObserveActiveWorkoutUseCase
import com.liftlog.app.feature.workout.domain.ObserveWorkoutTemplatesUseCase
import com.liftlog.app.feature.workout.domain.ObserveWorkoutPlansUseCase
import com.liftlog.app.feature.workout.domain.SaveActiveWorkoutAsTemplateUseCase
import com.liftlog.app.feature.workout.domain.StartWorkoutUseCase
import com.liftlog.app.feature.workout.domain.StartWorkoutTemplateUseCase
import com.liftlog.app.feature.workout.domain.UpdateSetUseCase
import com.liftlog.app.feature.workout.domain.UpdateActiveWorkoutDetailsUseCase
import com.liftlog.app.feature.workout.domain.UpdateWorkoutExerciseNotesUseCase
import com.liftlog.app.feature.workout.domain.DeleteWorkoutExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    observeActiveWorkoutUseCase: ObserveActiveWorkoutUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    observeWorkoutTemplatesUseCase: ObserveWorkoutTemplatesUseCase,
    observeWorkoutPlansUseCase: ObserveWorkoutPlansUseCase,
    private val ensureStarterExercisesUseCase: EnsureStarterExercisesUseCase,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val addExerciseToActiveWorkoutUseCase: AddExerciseToActiveWorkoutUseCase,
    private val addCustomExerciseUseCase: AddCustomExerciseUseCase,
    private val getRecentExercisePerformancesUseCase: GetRecentExercisePerformancesUseCase,
    private val saveActiveWorkoutAsTemplateUseCase: SaveActiveWorkoutAsTemplateUseCase,
    private val startWorkoutTemplateUseCase: StartWorkoutTemplateUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val discardWorkoutUseCase: DiscardWorkoutUseCase,
    private val updateActiveWorkoutDetailsUseCase: UpdateActiveWorkoutDetailsUseCase,
    private val updateWorkoutExerciseNotesUseCase: UpdateWorkoutExerciseNotesUseCase,
    private val deleteWorkoutExerciseUseCase: DeleteWorkoutExerciseUseCase,
    settingsRepository: SettingsRepository,
    gymLocationRepository: GymLocationRepository,
) : ViewModel() {
    private val exerciseHistory = MutableStateFlow<ExerciseHistoryDialogState?>(null)

    val uiState: StateFlow<WorkoutUiState> = combine(
        observeActiveWorkoutUseCase(),
        observeExercisesUseCase(""),
        observeWorkoutTemplatesUseCase(),
        observeWorkoutPlansUseCase(),
        gymLocationRepository.observeLocations(),
    ) { activeWorkout, exercises, templates, plans, locations ->
        WorkoutUiState(
            activeWorkout = activeWorkout,
            availableExercises = exercises,
            templates = templates,
            plans = plans,
            locations = locations,
        )
    }
        .combine(settingsRepository.settings) { state, settings ->
            state.copy(
                restTimerEnabled = settings.restTimerEnabled,
                restTimerOffsetSeconds = settings.restTimerOffsetSeconds,
            )
        }
        .combine(exerciseHistory) { state, history -> state.copy(exerciseHistory = history) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutUiState(),
        )

    init {
        viewModelScope.launch {
            ensureStarterExercisesUseCase()
        }
    }

    fun startWorkout(gymLocation: String?) {
        viewModelScope.launch {
            startWorkoutUseCase(gymLocation)
        }
    }

    fun saveActiveWorkoutAsTemplate(name: String) {
        viewModelScope.launch {
            saveActiveWorkoutAsTemplateUseCase(name)
        }
    }

    fun startTemplate(templateId: Long, gymLocation: String?) {
        viewModelScope.launch {
            startWorkoutTemplateUseCase(templateId, gymLocation)
        }
    }

    fun addExercise(exerciseId: Long, notes: String?) {
        viewModelScope.launch {
            addExerciseToActiveWorkoutUseCase(exerciseId, notes)
        }
    }

    fun addExercises(exerciseIds: List<Long>) {
        viewModelScope.launch {
            exerciseIds.forEach { exerciseId -> addExerciseToActiveWorkoutUseCase(exerciseId, null) }
        }
    }

    fun createAndAddExercise(draft: ExerciseDraft) {
        viewModelScope.launch {
            val exerciseId = addCustomExerciseUseCase(draft)
            addExerciseToActiveWorkoutUseCase(exerciseId, null)
        }
    }

    fun openExerciseHistory(exerciseId: Long, exerciseName: String) {
        viewModelScope.launch {
            exerciseHistory.value = ExerciseHistoryDialogState(
                exerciseName = exerciseName,
                performances = getRecentExercisePerformancesUseCase(exerciseId),
            )
        }
    }

    fun dismissExerciseHistory() {
        exerciseHistory.value = null
    }

    fun addSet(workoutExerciseId: Long, weight: Double, reps: Int) {
        viewModelScope.launch {
            addSetUseCase(
                workoutExerciseId = workoutExerciseId,
                weight = weight,
                reps = reps,
            )
        }
    }

    fun updateSet(setEntryId: Long, weight: Double, reps: Int) {
        viewModelScope.launch {
            updateSetUseCase(setEntryId, weight, reps)
        }
    }

    fun deleteSet(setEntryId: Long) {
        viewModelScope.launch {
            deleteSetUseCase(setEntryId)
        }
    }

    fun updateWorkoutDetails(gymLocation: String?, notes: String?) {
        viewModelScope.launch { updateActiveWorkoutDetailsUseCase(gymLocation, notes) }
    }

    fun updateWorkoutExerciseNotes(workoutExerciseId: Long, notes: String?) {
        viewModelScope.launch { updateWorkoutExerciseNotesUseCase(workoutExerciseId, notes) }
    }

    fun deleteWorkoutExercise(workoutExerciseId: Long) {
        viewModelScope.launch { deleteWorkoutExerciseUseCase(workoutExerciseId) }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            finishWorkoutUseCase()
        }
    }

    fun discardWorkout() {
        viewModelScope.launch {
            discardWorkoutUseCase()
        }
    }
}

data class WorkoutUiState(
    val activeWorkout: ActiveWorkout? = null,
    val availableExercises: List<Exercise> = emptyList(),
    val templates: List<WorkoutTemplate> = emptyList(),
    val plans: List<WorkoutPlan> = emptyList(),
    val locations: List<String> = emptyList(),
    val restTimerEnabled: Boolean = true,
    val restTimerOffsetSeconds: Int = 0,
    val exerciseHistory: ExerciseHistoryDialogState? = null,
)

data class ExerciseHistoryDialogState(
    val exerciseName: String,
    val performances: List<RecentExercisePerformance>,
)
