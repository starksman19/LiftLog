package com.liftlog.app.feature.exercises.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseDraft
import com.liftlog.app.feature.exercises.domain.EnsureStarterExercisesUseCase
import com.liftlog.app.feature.exercises.domain.AddCustomExerciseUseCase
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    observeExercisesUseCase: ObserveExercisesUseCase,
    private val ensureStarterExercisesUseCase: EnsureStarterExercisesUseCase,
    private val addCustomExerciseUseCase: AddCustomExerciseUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<ExerciseListUiState> = query
        .flatMapLatest { currentQuery ->
            observeExercisesUseCase(currentQuery).map { exercises ->
                ExerciseListUiState(
                    searchQuery = currentQuery,
                    exercises = exercises,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseListUiState(),
        )

    init {
        viewModelScope.launch {
            ensureStarterExercisesUseCase()
        }
    }

    fun onSearchQueryChanged(value: String) {
        query.update { value }
    }

    fun addCustomExercise(draft: ExerciseDraft) {
        viewModelScope.launch {
            addCustomExerciseUseCase(draft)
        }
    }
}

data class ExerciseListUiState(
    val searchQuery: String = "",
    val exercises: List<Exercise> = emptyList(),
)
