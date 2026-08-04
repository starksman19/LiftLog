package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.RecentExercisePerformance
import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.feature.exercises.domain.EnsureStarterExercisesUseCase
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.workout.domain.AddExerciseToActiveWorkoutUseCase
import com.liftlog.app.feature.workout.domain.AddSetUseCase
import com.liftlog.app.feature.workout.domain.DiscardWorkoutUseCase
import com.liftlog.app.feature.workout.domain.DeleteSetUseCase
import com.liftlog.app.feature.workout.domain.FinishWorkoutUseCase
import com.liftlog.app.feature.workout.domain.GetRecentExercisePerformancesUseCase
import com.liftlog.app.feature.workout.domain.ObserveActiveWorkoutUseCase
import com.liftlog.app.feature.workout.domain.ObserveWorkoutTemplatesUseCase
import com.liftlog.app.feature.workout.domain.SaveActiveWorkoutAsTemplateUseCase
import com.liftlog.app.feature.workout.domain.StartWorkoutUseCase
import com.liftlog.app.feature.workout.domain.StartWorkoutTemplateUseCase
import com.liftlog.app.feature.workout.domain.UpdateSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    observeActiveWorkoutUseCase: ObserveActiveWorkoutUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    observeWorkoutTemplatesUseCase: ObserveWorkoutTemplatesUseCase,
    private val ensureStarterExercisesUseCase: EnsureStarterExercisesUseCase,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val addExerciseToActiveWorkoutUseCase: AddExerciseToActiveWorkoutUseCase,
    private val getRecentExercisePerformancesUseCase: GetRecentExercisePerformancesUseCase,
    private val saveActiveWorkoutAsTemplateUseCase: SaveActiveWorkoutAsTemplateUseCase,
    private val startWorkoutTemplateUseCase: StartWorkoutTemplateUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val discardWorkoutUseCase: DiscardWorkoutUseCase,
) : ViewModel() {
    private val exercisePendingAddition = MutableStateFlow<Exercise?>(null)
    private val recentPerformances = MutableStateFlow<List<RecentExercisePerformance>>(emptyList())

    val uiState: StateFlow<WorkoutUiState> = combine(
        observeActiveWorkoutUseCase(),
        observeExercisesUseCase(""),
        exercisePendingAddition,
        recentPerformances,
        observeWorkoutTemplatesUseCase(),
    ) { activeWorkout, exercises, pendingExercise, recent, templates ->
        WorkoutUiState(
            activeWorkout = activeWorkout,
            availableExercises = exercises
                .filter { exercise ->
                    exercise.category == ExerciseCategory.FreeWeights ||
                        exercise.gymLocation.isNullOrBlank() ||
                        exercise.gymLocation.equals(activeWorkout?.gymLocation, ignoreCase = true)
                }
                .take(8),
            exercisePendingAddition = pendingExercise,
            recentPerformances = recent,
            templates = templates,
        )
    }
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

    fun openAddExercise(exercise: Exercise) {
        exercisePendingAddition.value = exercise
        viewModelScope.launch {
            recentPerformances.value = getRecentExercisePerformancesUseCase(exercise.id)
        }
    }

    fun dismissAddExercise() {
        exercisePendingAddition.value = null
        recentPerformances.value = emptyList()
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
    val exercisePendingAddition: Exercise? = null,
    val recentPerformances: List<RecentExercisePerformance> = emptyList(),
    val templates: List<WorkoutTemplate> = emptyList(),
)
