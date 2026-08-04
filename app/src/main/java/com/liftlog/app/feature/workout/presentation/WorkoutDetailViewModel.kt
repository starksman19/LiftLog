package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.WorkoutDetail
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.workout.domain.AddExerciseToWorkoutUseCase
import com.liftlog.app.feature.workout.domain.AddSetUseCase
import com.liftlog.app.feature.workout.domain.DeleteCompletedWorkoutUseCase
import com.liftlog.app.feature.workout.domain.DeleteSetUseCase
import com.liftlog.app.feature.workout.domain.DeleteWorkoutExerciseUseCase
import com.liftlog.app.feature.workout.domain.ObserveWorkoutDetailUseCase
import com.liftlog.app.feature.workout.domain.UpdateCompletedWorkoutUseCase
import com.liftlog.app.feature.workout.domain.UpdateSetUseCase
import com.liftlog.app.feature.workout.domain.UpdateWorkoutExerciseNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val observeWorkoutDetailUseCase: ObserveWorkoutDetailUseCase,
    private val observeExercisesUseCase: ObserveExercisesUseCase,
    private val updateCompletedWorkoutUseCase: UpdateCompletedWorkoutUseCase,
    private val deleteCompletedWorkoutUseCase: DeleteCompletedWorkoutUseCase,
    private val addExerciseToWorkoutUseCase: AddExerciseToWorkoutUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val deleteSetUseCase: DeleteSetUseCase,
    private val updateWorkoutExerciseNotesUseCase: UpdateWorkoutExerciseNotesUseCase,
    private val deleteWorkoutExerciseUseCase: DeleteWorkoutExerciseUseCase,
) : ViewModel() {
    fun uiState(workoutId: Long): StateFlow<WorkoutDetailUiState> = combine(
        observeWorkoutDetailUseCase(workoutId),
        observeExercisesUseCase(""),
    ) { workout, exercises ->
        WorkoutDetailUiState(workout = workout, availableExercises = exercises)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutDetailUiState(),
    )

    fun updateWorkout(workout: WorkoutDetail, startedAt: Long, finishedAt: Long, gym: String?, notes: String?) {
        viewModelScope.launch { updateCompletedWorkoutUseCase(workout.id, startedAt, finishedAt, gym, notes) }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch { deleteCompletedWorkoutUseCase(workoutId) }
    }

    fun addExercise(workoutId: Long, exerciseId: Long) {
        viewModelScope.launch { addExerciseToWorkoutUseCase(workoutId, exerciseId, null) }
    }

    fun addSet(workoutExerciseId: Long, weight: Double, reps: Int) {
        viewModelScope.launch { addSetUseCase(workoutExerciseId, weight, reps) }
    }

    fun updateSet(setId: Long, weight: Double, reps: Int) {
        viewModelScope.launch { updateSetUseCase(setId, weight, reps) }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { deleteSetUseCase(setId) }
    }

    fun updateExerciseNotes(workoutExerciseId: Long, notes: String?) {
        viewModelScope.launch { updateWorkoutExerciseNotesUseCase(workoutExerciseId, notes) }
    }

    fun deleteExercise(workoutExerciseId: Long) {
        viewModelScope.launch { deleteWorkoutExerciseUseCase(workoutExerciseId) }
    }
}

data class WorkoutDetailUiState(
    val workout: WorkoutDetail? = null,
    val availableExercises: List<Exercise> = emptyList(),
)
