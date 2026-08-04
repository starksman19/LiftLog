package com.liftlog.app.feature.settings.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.feature.backup.domain.ExportBackupUseCase
import com.liftlog.app.feature.backup.domain.ImportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
) : ViewModel() {
    private val operation = MutableStateFlow(BackupOperationState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        operation,
    ) { settings, operationState ->
        SettingsUiState(
            settings = settings,
            isWorking = operationState.isWorking,
            message = operationState.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { settingsRepository.setWeightUnit(unit) }
    }

    fun setDefaultRestSeconds(seconds: Int) {
        viewModelScope.launch { settingsRepository.setDefaultRestSeconds(seconds) }
    }

    fun exportTo(uri: Uri) = runBackupOperation(
        action = { exportBackupUseCase(uri) },
        success = { summary -> "Exported ${summary.workouts} workouts and ${summary.sets} sets." },
    )

    fun importFrom(uri: Uri) = runBackupOperation(
        action = { importBackupUseCase(uri) },
        success = { summary -> "Imported ${summary.workouts} workouts and ${summary.sets} sets." },
    )

    fun clearMessage() {
        operation.update { it.copy(message = null) }
    }

    private fun runBackupOperation(
        action: suspend () -> com.liftlog.app.feature.backup.domain.BackupSummary,
        success: (com.liftlog.app.feature.backup.domain.BackupSummary) -> String,
    ) {
        if (operation.value.isWorking) return
        viewModelScope.launch {
            operation.value = BackupOperationState(isWorking = true)
            val message = runCatching { action() }
                .fold(
                    onSuccess = success,
                    onFailure = { "Backup failed: ${it.message ?: "unknown error"}" },
                )
            operation.value = BackupOperationState(message = message)
        }
    }
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isWorking: Boolean = false,
    val message: String? = null,
)

private data class BackupOperationState(
    val isWorking: Boolean = false,
    val message: String? = null,
)
