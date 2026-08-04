package com.liftlog.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.HistoricalSet
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.progress.domain.ObserveExerciseHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseHistoryViewModel @Inject constructor(
    private val observeExercisesUseCase: ObserveExercisesUseCase,
    private val observeExerciseHistoryUseCase: ObserveExerciseHistoryUseCase,
) : ViewModel() {
    fun uiState(exerciseId: Long): StateFlow<ExerciseHistoryUiState> = combine(
        observeExercisesUseCase("").map { exercises -> exercises.firstOrNull { it.id == exerciseId } },
        observeExerciseHistoryUseCase(exerciseId),
    ) { exercise, sets ->
        ExerciseHistoryUiState(
            exerciseName = exercise?.name.orEmpty(),
            exercise = exercise,
            history = sets.groupBy { it.workoutSessionId }
                .map { (_, sessionSets) ->
                    ExerciseHistorySession(
                        finishedAtEpochMillis = sessionSets.first().finishedAtEpochMillis,
                        sets = sessionSets,
                    )
                },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseHistoryUiState(),
    )
}

data class ExerciseHistoryUiState(
    val exerciseName: String = "",
    val exercise: Exercise? = null,
    val history: List<ExerciseHistorySession> = emptyList(),
)

data class ExerciseHistorySession(
    val finishedAtEpochMillis: Long,
    val sets: List<HistoricalSet>,
)
