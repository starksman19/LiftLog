package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.feature.exercises.domain.EnsureStarterExercisesUseCase
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.workout.domain.AddExerciseToActiveWorkoutUseCase
import com.liftlog.app.feature.workout.domain.AddSetUseCase
import com.liftlog.app.feature.workout.domain.DiscardWorkoutUseCase
import com.liftlog.app.feature.workout.domain.FinishWorkoutUseCase
import com.liftlog.app.feature.workout.domain.ObserveActiveWorkoutUseCase
import com.liftlog.app.feature.workout.domain.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    observeActiveWorkoutUseCase: ObserveActiveWorkoutUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    private val ensureStarterExercisesUseCase: EnsureStarterExercisesUseCase,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val addExerciseToActiveWorkoutUseCase: AddExerciseToActiveWorkoutUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val discardWorkoutUseCase: DiscardWorkoutUseCase,
) : ViewModel() {
    val uiState: StateFlow<WorkoutUiState> = combine(
        observeActiveWorkoutUseCase(),
        observeExercisesUseCase(""),
    ) { activeWorkout, exercises ->
        WorkoutUiState(
            activeWorkout = activeWorkout,
            availableExercises = exercises.take(8),
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

    fun startWorkout() {
        viewModelScope.launch {
            startWorkoutUseCase()
        }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            addExerciseToActiveWorkoutUseCase(exerciseId)
        }
    }

    fun addQuickSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            val loggedExercise = uiState.value.activeWorkout
                ?.exercises
                ?.firstOrNull { it.id == workoutExerciseId }
                ?: return@launch
            val previousSet = loggedExercise.sets.lastOrNull()

            addSetUseCase(
                workoutExerciseId = workoutExerciseId,
                weight = previousSet?.weight ?: 0.0,
                reps = previousSet?.reps ?: 10,
            )
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
)

