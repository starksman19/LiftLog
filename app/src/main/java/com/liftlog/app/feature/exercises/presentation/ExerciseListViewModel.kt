package com.liftlog.app.feature.exercises.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseDraft
import com.liftlog.app.core.util.PolishTextComparator
import com.liftlog.app.feature.exercises.domain.EnsureStarterExercisesUseCase
import com.liftlog.app.feature.exercises.domain.AddCustomExerciseUseCase
import com.liftlog.app.feature.exercises.domain.DeleteExerciseUseCase
import com.liftlog.app.feature.exercises.domain.ObserveExercisesUseCase
import com.liftlog.app.feature.exercises.domain.UpdateExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
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
    private val updateExerciseUseCase: UpdateExerciseUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sortMode = MutableStateFlow(ExerciseSortMode.NameAscending)

    val uiState: StateFlow<ExerciseListUiState> = combine(query, sortMode) { currentQuery, currentSortMode ->
        currentQuery to currentSortMode
    }.flatMapLatest { (currentQuery, currentSortMode) ->
        observeExercisesUseCase(currentQuery).map { exercises ->
            ExerciseListUiState(
                searchQuery = currentQuery,
                sortMode = currentSortMode,
                exercises = exercises.sortedWith(currentSortMode.comparator),
            )
        }
    }.stateIn(
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

    fun onSortModeChanged(value: ExerciseSortMode) {
        sortMode.value = value
    }

    fun addCustomExercise(draft: ExerciseDraft) {
        viewModelScope.launch {
            addCustomExerciseUseCase(draft)
        }
    }

    fun updateExercise(exerciseId: Long, draft: ExerciseDraft) {
        viewModelScope.launch { updateExerciseUseCase(exerciseId, draft) }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch { deleteExerciseUseCase(exerciseId) }
    }
}

data class ExerciseListUiState(
    val searchQuery: String = "",
    val sortMode: ExerciseSortMode = ExerciseSortMode.NameAscending,
    val exercises: List<Exercise> = emptyList(),
)

enum class ExerciseSortMode(val comparator: Comparator<Exercise>) {
    NameAscending(Comparator { first, second -> PolishTextComparator.compare(first.name, second.name) }),
    NameDescending(Comparator { first, second -> PolishTextComparator.compare(second.name, first.name) }),
    Category(Comparator { first, second ->
        first.category.name.compareTo(second.category.name)
            .takeUnless { it == 0 }
            ?: PolishTextComparator.compare(first.name, second.name)
    }),
}
