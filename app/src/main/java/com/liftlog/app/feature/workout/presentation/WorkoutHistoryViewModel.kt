package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.WorkoutSummary
import com.liftlog.app.feature.workout.domain.ObserveCompletedWorkoutsUseCase
import com.liftlog.app.feature.workout.domain.DeleteCompletedWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    observeCompletedWorkoutsUseCase: ObserveCompletedWorkoutsUseCase,
    private val deleteCompletedWorkoutUseCase: DeleteCompletedWorkoutUseCase,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val gymFilter = MutableStateFlow<String?>(null)
    private val dateFilter = MutableStateFlow("")
    private val sortMode = MutableStateFlow(WorkoutSortMode.NewestFirst)

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        observeCompletedWorkoutsUseCase(),
        searchQuery,
        gymFilter,
        dateFilter,
        sortMode,
    ) { workouts, query, selectedGym, dateQuery, currentSortMode ->
        val gyms = workouts.mapNotNull { it.gymLocation }.distinct().sorted()
        WorkoutHistoryUiState(
            searchQuery = query,
            selectedGym = selectedGym,
            dateFilter = dateQuery,
            sortMode = currentSortMode,
            gyms = gyms,
            workouts = workouts.filter { workout ->
                val matchesGym = selectedGym == null || workout.gymLocation.equals(selectedGym, ignoreCase = true)
                val searchable = listOfNotNull(workout.gymLocation, workout.notes, workout.exerciseNames).joinToString(" ")
                val dateText = java.time.Instant.ofEpochMilli(workout.finishedAtEpochMillis)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                matchesGym &&
                    (query.isBlank() || searchable.contains(query, ignoreCase = true)) &&
                    (dateQuery.isBlank() || dateText.startsWith(dateQuery))
            }.sortedWith(currentSortMode.comparator),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutHistoryUiState(),
    )

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun selectGym(gym: String?) {
        gymFilter.value = gym
    }

    fun updateDateFilter(value: String) {
        dateFilter.value = value
    }

    fun updateSortMode(value: WorkoutSortMode) {
        sortMode.value = value
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch { deleteCompletedWorkoutUseCase(workoutId) }
    }
}

data class WorkoutHistoryUiState(
    val searchQuery: String = "",
    val selectedGym: String? = null,
    val dateFilter: String = "",
    val sortMode: WorkoutSortMode = WorkoutSortMode.NewestFirst,
    val gyms: List<String> = emptyList(),
    val workouts: List<WorkoutSummary> = emptyList(),
)

enum class WorkoutSortMode(val comparator: Comparator<WorkoutSummary>) {
    NewestFirst(compareByDescending { it.finishedAtEpochMillis }),
    OldestFirst(compareBy { it.finishedAtEpochMillis }),
    Location(compareBy<WorkoutSummary, String>(String.CASE_INSENSITIVE_ORDER) { it.gymLocation.orEmpty() }
        .thenByDescending { it.finishedAtEpochMillis }),
}
