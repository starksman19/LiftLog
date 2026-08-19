package com.liftlog.app.feature.settings.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.AppLanguage
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.core.model.RestTimerMode
import com.liftlog.app.core.ui.localization.localizedNow
import com.liftlog.app.core.ui.localization.AppLanguageState
import com.liftlog.app.feature.backup.domain.ExportBackupUseCase
import com.liftlog.app.feature.backup.domain.BackupContents
import com.liftlog.app.feature.backup.domain.BackupSelection
import com.liftlog.app.feature.backup.domain.InspectBackupUseCase
import com.liftlog.app.feature.backup.domain.ImportBackupUseCase
import com.liftlog.app.feature.locations.domain.GymLocationRepository
import com.liftlog.app.feature.report.domain.ExportTrainingReportUseCase
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
import java.time.LocalDate

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val inspectBackupUseCase: InspectBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val exportTrainingReportUseCase: ExportTrainingReportUseCase,
    private val gymLocationRepository: GymLocationRepository,
) : ViewModel() {
    private val operation = MutableStateFlow(BackupOperationState())
    private val language = MutableStateFlow(AppLanguageState.current)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        operation,
        gymLocationRepository.observeLocations(),
        language,
    ) { settings, operationState, locations, selectedLanguage ->
        SettingsUiState(
            settings = settings,
            isWorking = operationState.isWorking,
            message = operationState.message,
            importPreview = operationState.importPreview,
            locations = locations,
            language = selectedLanguage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { settingsRepository.setWeightUnit(unit) }
    }

    fun setRestTimerMode(mode: RestTimerMode) {
        viewModelScope.launch { settingsRepository.setRestTimerMode(mode) }
    }

    fun setRestTimerOffsetSeconds(seconds: Int) {
        viewModelScope.launch { settingsRepository.setRestTimerOffsetSeconds(seconds) }
    }

    fun setLanguage(language: AppLanguage) {
        AppLanguageState.set(language)
        this.language.value = language
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

    fun exportTrainingReportTo(uri: Uri, startDate: LocalDate, endDate: LocalDate) = runTrainingReportOperation(
        action = { exportTrainingReportUseCase(uri, startDate, endDate, AppLanguageState.current) },
        success = { summary ->
            localizedNow(
                "Exported ${summary.workouts} workouts, ${summary.exercises} exercise entries, and ${summary.sets} sets to Excel.",
                "Wyeksportowano do Excela: ${summary.workouts} treningów, ${summary.exercises} wpisów ćwiczeń i ${summary.sets} serii.",
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

    private fun runTrainingReportOperation(
        action: suspend () -> com.liftlog.app.feature.report.domain.TrainingReportSummary,
        success: (com.liftlog.app.feature.report.domain.TrainingReportSummary) -> String,
    ) {
        if (operation.value.isWorking) return
        viewModelScope.launch {
            operation.value = BackupOperationState(isWorking = true)
            val message = runCatching { action() }
                .fold(
                    onSuccess = success,
                    onFailure = {
                        localizedNow(
                            "Excel export failed: ${it.message ?: "unknown error"}",
                            "Eksport do Excela nie powiódł się: ${it.message ?: "nieznany błąd"}",
                        )
                    },
                )
            operation.value = BackupOperationState(message = message)
        }
    }

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
