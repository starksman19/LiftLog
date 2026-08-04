package com.liftlog.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import com.liftlog.app.feature.progress.domain.ObserveProgressDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    observeProgressDashboardUseCase: ObserveProgressDashboardUseCase,
) : ViewModel() {
    private val range = MutableStateFlow(7)

    val uiState: StateFlow<ProgressUiState> = range.flatMapLatest { selectedRange ->
        observeProgressDashboardUseCase(selectedRange).map { dashboard ->
            ProgressUiState(
                selectedRange = selectedRange,
                recentVolumes = dashboard.recentVolumes,
                exercises = dashboard.exercises,
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgressUiState(),
        )

    fun setRange(workoutCount: Int) {
        range.value = workoutCount
    }
}

data class ProgressUiState(
    val selectedRange: Int = 7,
    val recentVolumes: List<SessionVolume> = emptyList(),
    val exercises: List<ExerciseProgress> = emptyList(),
)
