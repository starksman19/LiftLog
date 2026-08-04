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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProgressViewModel @Inject constructor(
    observeProgressDashboardUseCase: ObserveProgressDashboardUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProgressUiState> = observeProgressDashboardUseCase()
        .map { dashboard ->
            ProgressUiState(
                recentVolumes = dashboard.recentVolumes,
                exercises = dashboard.exercises,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgressUiState(),
        )
}

data class ProgressUiState(
    val recentVolumes: List<SessionVolume> = emptyList(),
    val exercises: List<ExerciseProgress> = emptyList(),
)
