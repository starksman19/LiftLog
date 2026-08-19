package com.liftlog.app.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.WorkoutSummary
import com.liftlog.app.core.util.PolishTextComparator
import com.liftlog.app.feature.workout.domain.ObserveCompletedWorkoutsUseCase
import com.liftlog.app.feature.workout.domain.DeleteCompletedWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    private val dateRange = MutableStateFlow(HistoryDateRange(endDate = LocalDate.now().toString()))
    private val sortMode = MutableStateFlow(WorkoutSortMode.NewestFirst)

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        observeCompletedWorkoutsUseCase(),
        searchQuery,
        gymFilter,
        dateRange,
        sortMode,
    ) { workouts, query, selectedGym, selectedDateRange, currentSortMode ->
        val gyms = workouts.mapNotNull { it.gymLocation }.distinct().sortedWith(PolishTextComparator)
        WorkoutHistoryUiState(
            searchQuery = query,
            selectedGym = selectedGym,
            dateRange = selectedDateRange,
            sortMode = currentSortMode,
            gyms = gyms,
            workouts = workouts.filter { workout ->
                val matchesGym = selectedGym == null || workout.gymLocation.equals(selectedGym, ignoreCase = true)
                val searchable = listOfNotNull(workout.gymLocation, workout.notes, workout.exerciseNames).joinToString(" ")
                val workoutDate = Instant.ofEpochMilli(workout.finishedAtEpochMillis)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val startDate = selectedDateRange.startDate?.toLocalDateOrNull()
                val endDate = selectedDateRange.endDate.toLocalDateOrNull()
                matchesGym &&
                    (query.isBlank() || searchable.contains(query, ignoreCase = true)) &&
                    (startDate == null || !workoutDate.isBefore(startDate)) &&
                    (endDate == null || !workoutDate.isAfter(endDate))
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

    fun updateDateRange(value: HistoryDateRange) {
        dateRange.value = value
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
    val dateRange: HistoryDateRange = HistoryDateRange(endDate = LocalDate.now().toString()),
    val sortMode: WorkoutSortMode = WorkoutSortMode.NewestFirst,
    val gyms: List<String> = emptyList(),
    val workouts: List<WorkoutSummary> = emptyList(),
)

data class HistoryDateRange(
    val startDate: String? = null,
    val endDate: String,
)

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

enum class WorkoutSortMode(val comparator: Comparator<WorkoutSummary>) {
    NewestFirst(compareByDescending { it.finishedAtEpochMillis }),
    OldestFirst(compareBy { it.finishedAtEpochMillis }),
    Location(Comparator { first, second ->
        PolishTextComparator.compare(first.gymLocation.orEmpty(), second.gymLocation.orEmpty())
            .takeUnless { it == 0 }
            ?: second.finishedAtEpochMillis.compareTo(first.finishedAtEpochMillis)
    }),
}
