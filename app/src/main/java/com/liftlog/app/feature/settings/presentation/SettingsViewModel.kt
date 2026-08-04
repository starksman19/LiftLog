package com.liftlog.app.feature.settings.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.AppLanguage
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.core.ui.localization.localizedNow
import com.liftlog.app.feature.backup.domain.ExportBackupUseCase
import com.liftlog.app.feature.backup.domain.BackupContents
import com.liftlog.app.feature.backup.domain.BackupSelection
import com.liftlog.app.feature.backup.domain.InspectBackupUseCase
import com.liftlog.app.feature.backup.domain.ImportBackupUseCase
import com.liftlog.app.feature.locations.domain.GymLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
    private val inspectBackupUseCase: InspectBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val gymLocationRepository: GymLocationRepository,
) : ViewModel() {
    private val operation = MutableStateFlow(BackupOperationState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        operation,
        gymLocationRepository.observeLocations(),
    ) { settings, operationState, locations ->
        SettingsUiState(
            settings = settings,
            isWorking = operationState.isWorking,
            message = operationState.message,
            importPreview = operationState.importPreview,
            locations = locations,
            language = currentLanguage(),
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

    fun setLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
    }

    fun exportTo(uri: Uri, selection: BackupSelection) = runBackupOperation(
        action = { exportBackupUseCase(uri, selection) },
        success = { summary ->
            localizedNow(
                "Exported ${summary.workouts} workouts and ${summary.sets} sets.",
                "Wyeksportowano ${summary.workouts} treningów i ${summary.sets} serii.",
            )
        },
    )

    fun inspectImport(uri: Uri) {
        if (operation.value.isWorking) return
        viewModelScope.launch {
            operation.value = BackupOperationState(isWorking = true)
            val result = runCatching { inspectBackupUseCase(uri) }
            operation.value = result.fold(
                onSuccess = { contents -> BackupOperationState(importPreview = ImportPreview(uri, contents)) },
                onFailure = { error ->
                    BackupOperationState(
                        message = localizedNow(
                            "Backup failed: ${error.message ?: "unknown error"}",
                            "Operacja na kopii zapasowej nie powiodła się: ${error.message ?: "nieznany błąd"}",
                        ),
                    )
                },
            )
        }
    }

    fun dismissImportPreview() {
        operation.update { it.copy(importPreview = null) }
    }

    fun importFrom(uri: Uri) = runBackupOperation(
        action = { importBackupUseCase(uri) },
        success = { summary ->
            localizedNow(
                "Imported ${summary.workouts} workouts and ${summary.sets} sets.",
                "Zaimportowano ${summary.workouts} treningów i ${summary.sets} serii.",
            )
        },
    )

    fun clearMessage() {
        operation.update { it.copy(message = null) }
    }

    fun renameLocation(oldName: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { gymLocationRepository.rename(oldName, newName.trim()) }
    }

    fun deleteLocation(name: String) {
        viewModelScope.launch { gymLocationRepository.delete(name) }
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
                    onFailure = {
                        localizedNow(
                            "Backup failed: ${it.message ?: "unknown error"}",
                            "Operacja na kopii zapasowej nie powiodła się: ${it.message ?: "nieznany błąd"}",
                        )
                    },
                )
            operation.value = BackupOperationState(message = message)
        }
    }

    private fun currentLanguage(): AppLanguage =
        if (AppCompatDelegate.getApplicationLocales().get(0)?.language == "pl") AppLanguage.Polish else AppLanguage.English
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isWorking: Boolean = false,
    val message: String? = null,
    val importPreview: ImportPreview? = null,
    val locations: List<String> = emptyList(),
    val language: AppLanguage = AppLanguage.English,
)

data class ImportPreview(
    val uri: Uri,
    val contents: BackupContents,
)

private data class BackupOperationState(
    val isWorking: Boolean = false,
    val message: String? = null,
    val importPreview: ImportPreview? = null,
)
