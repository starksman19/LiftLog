package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.WorkoutSummary
import com.liftlog.app.feature.workout.domain.ObserveCompletedWorkoutsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    observeCompletedWorkoutsUseCase: ObserveCompletedWorkoutsUseCase,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val gymFilter = MutableStateFlow<String?>(null)
    private val dateFilter = MutableStateFlow("")

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        observeCompletedWorkoutsUseCase(),
        searchQuery,
        gymFilter,
        dateFilter,
    ) { workouts, query, selectedGym, dateQuery ->
        val gyms = workouts.mapNotNull { it.gymLocation }.distinct().sorted()
        WorkoutHistoryUiState(
            searchQuery = query,
            selectedGym = selectedGym,
            dateFilter = dateQuery,
            gyms = gyms,
            workouts = workouts.filter { workout ->
                val matchesGym = selectedGym == null || workout.gymLocation.equals(selectedGym, ignoreCase = true)
                val searchable = listOfNotNull(workout.gymLocation, workout.notes, workout.exerciseNames).joinToString(" ")
                val dateText = java.time.Instant.ofEpochMilli(workout.finishedAtEpochMillis)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                matchesGym &&
                    (query.isBlank() || searchable.contains(query, ignoreCase = true)) &&
                    (dateQuery.isBlank() || dateText.startsWith(dateQuery))
            },
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
}

data class WorkoutHistoryUiState(
    val searchQuery: String = "",
    val selectedGym: String? = null,
    val dateFilter: String = "",
    val gyms: List<String> = emptyList(),
    val workouts: List<WorkoutSummary> = emptyList(),
)
