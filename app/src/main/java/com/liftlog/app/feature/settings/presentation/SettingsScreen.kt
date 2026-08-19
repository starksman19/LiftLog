package com.liftlog.app.feature.settings.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.core.model.AppLanguage
import com.liftlog.app.core.model.RestTimerMode
import com.liftlog.app.feature.backup.domain.BackupSection
import com.liftlog.app.feature.backup.domain.BackupSelection
import com.liftlog.app.core.ui.localization.t
import java.time.LocalDate

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onWeightUnitChanged = viewModel::setWeightUnit,
        onRestTimerModeChanged = viewModel::setRestTimerMode,
        onRestTimerOffsetChanged = viewModel::setRestTimerOffsetSeconds,
        onLanguageChanged = viewModel::setLanguage,
        onExport = viewModel::exportTo,
        onExportTrainingReport = viewModel::exportTrainingReportTo,
        onInspectImport = viewModel::inspectImport,
        onImport = viewModel::importFrom,
        onDismissImportPreview = viewModel::dismissImportPreview,
        onMessageShown = viewModel::clearMessage,
        onRenameLocation = viewModel::renameLocation,
        onDeleteLocation = viewModel::deleteLocation,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    onRestTimerModeChanged: (RestTimerMode) -> Unit,
    onRestTimerOffsetChanged: (Int) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onExport: (android.net.Uri, BackupSelection) -> Unit,
    onExportTrainingReport: (android.net.Uri, LocalDate, LocalDate) -> Unit,
    onInspectImport: (android.net.Uri) -> Unit,
    onImport: (android.net.Uri) -> Unit,
    onDismissImportPreview: () -> Unit,
    onMessageShown: () -> Unit,
    onRenameLocation: (String, String) -> Unit,
    onDeleteLocation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var exportDialogVisible by remember { mutableStateOf(false) }
    var trainingReportDialogVisible by remember { mutableStateOf(false) }
    var exportSelection by remember { mutableStateOf(BackupSelection.Everything) }
    var pendingTrainingReportRange by remember {
        mutableStateOf(TrainingReportDateRange(LocalDate.now().minusMonths(1), LocalDate.now()))
    }
    var locationPendingEdit by remember { mutableStateOf<String?>(null) }
    var locationPendingDelete by remember { mutableStateOf<String?>(null) }
    var restTimerOffsetText by remember(state.settings.restTimerOffsetSeconds) {
        mutableStateOf(state.settings.restTimerOffsetSeconds.toString())
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { onExport(it, exportSelection) } },
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(onInspectImport) },
    )
    val trainingReportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        ),
        onResult = { uri ->
            uri?.let {
                onExportTrainingReport(it, pendingTrainingReportRange.startDate, pendingTrainingReportRange.endDate)
            }
        },
    )

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    text = t("Settings"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                SettingsSection(title = t("Language")) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppLanguage.entries.forEachIndexed { index, language ->
                            SegmentedButton(
                                selected = state.language == language,
                                onClick = { onLanguageChanged(language) },
                                shape = SegmentedButtonDefaults.itemShape(index, AppLanguage.entries.size),
                                label = { Text(t(if (language == AppLanguage.English) "English" else "Polish")) },
                            )
                        }
                    }
                }
            }
            item {
                SettingsSection(title = t("Units")) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        WeightUnit.entries.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = state.settings.weightUnit == unit,
                                onClick = { onWeightUnitChanged(unit) },
                                shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                                label = { Text(t(if (unit == WeightUnit.Kilograms) "Kilograms" else "Pounds")) },
                            )
                        }
                    }
                }
            }
            item {
                SettingsSection(title = t("Rest timer", "Timer przerwy")) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        RestTimerMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = state.settings.restTimerMode == mode,
                                onClick = { onRestTimerModeChanged(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, RestTimerMode.entries.size),
                                label = {
                                    Text(
                                        when (mode) {
                                            RestTimerMode.Workout -> t("Workout", "Trening")
                                            RestTimerMode.Exercise -> t("Exercise", "Ćwiczenie")
                                            RestTimerMode.Off -> t("Off", "Wyłączony")
                                        },
                                    )
                                },
                            )
                        }
                    }
                    Text(
                        text = when (state.settings.restTimerMode) {
                            RestTimerMode.Workout -> t("One timer below the workout header.", "Jeden timer pod nagłówkiem treningu.")
                            RestTimerMode.Exercise -> t("A small timer on each exercised card.", "Mały timer na każdym kafelku ćwiczenia.")
                            RestTimerMode.Off -> t("Timers are hidden everywhere.", "Timery są ukryte wszędzie.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = restTimerOffsetText,
                        onValueChange = { value ->
                            if (value.all(Char::isDigit)) {
                                restTimerOffsetText = value
                                value.toIntOrNull()?.let(onRestTimerOffsetChanged)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.settings.restTimerMode != RestTimerMode.Off,
                        label = { Text(t("Timer offset (seconds)", "Offset timera (sekundy)")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            if (state.locations.isNotEmpty()) {
                item {
                    SettingsSection(title = t("Gym locations", "Lokalizacje siłowni")) {
                        state.locations.forEach { location ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Text(location)
                                Row {
                                    IconButton(onClick = { locationPendingEdit = location }) {
                                        androidx.compose.material3.Icon(Icons.Outlined.Edit, t("Rename location"))
                                    }
                                    IconButton(onClick = { locationPendingDelete = location }) {
                                        androidx.compose.material3.Icon(Icons.Outlined.Delete, t("Delete location", "Usuń lokalizację"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                SettingsSection(title = t("Data transfer")) {
                    OutlinedButton(
                        onClick = { exportDialogVisible = true },
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.FileDownload, contentDescription = null)
                        Text(t("Export backup"), modifier = Modifier.padding(start = 8.dp))
                    }
                    OutlinedButton(
                        onClick = { trainingReportDialogVisible = true },
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.FileDownload, contentDescription = null)
                        Text(
                            t("Export training report (Excel)", "Eksportuj raport treningowy (Excel)"),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.FileUpload, contentDescription = null)
                        Text(t("Import backup"), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }

    if (exportDialogVisible) {
        ExportSelectionDialog(
            selection = exportSelection,
            onSelectionChanged = { exportSelection = it },
            onDismiss = { exportDialogVisible = false },
            onConfirm = {
                exportDialogVisible = false
                exportLauncher.launch("liftlog-backup.json")
            },
        )
    }

    if (trainingReportDialogVisible) {
        TrainingReportExportDialog(
            onDismiss = { trainingReportDialogVisible = false },
            onConfirm = { range ->
                pendingTrainingReportRange = range
                trainingReportDialogVisible = false
                trainingReportLauncher.launch(
                    "liftlog-training-report-${range.startDate}-to-${range.endDate}.xlsx",
                )
            },
        )
    }

    state.importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = onDismissImportPreview,
            title = { Text(t("Import backup")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("The file contains:", "Plik zawiera:"))
                    BackupSection.entries
                        .filter(preview.contents.selection::includes)
                        .forEach { section -> Text(t(section.label)) }
                    Text(
                        text = t(
                            "${preview.contents.summary.exercises} exercises, ${preview.contents.summary.workouts} workouts, ${preview.contents.summary.sets} sets, ${preview.contents.summary.templates} templates",
                            "${preview.contents.summary.exercises} ćwiczeń, ${preview.contents.summary.workouts} treningów, ${preview.contents.summary.sets} serii, ${preview.contents.summary.templates} szablonów",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onImport(preview.uri)
                        onDismissImportPreview()
                    },
                ) { Text(t("Import selected data", "Importuj wybrane dane")) }
            },
            dismissButton = {
                TextButton(onClick = onDismissImportPreview) { Text(t("Cancel")) }
            },
        )
    }

    locationPendingEdit?.let { oldName ->
        EditLocationDialog(
            oldName = oldName,
            onDismiss = { locationPendingEdit = null },
            onSave = { newName ->
                onRenameLocation(oldName, newName)
                locationPendingEdit = null
            },
        )
    }
    locationPendingDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { locationPendingDelete = null },
            title = { Text(t("Delete $name?", "Usunąć $name?") ) },
            text = { Text(t("This removes the location from machines and workout records, but does not delete the workouts.", "Spowoduje to usunięcie lokalizacji z maszyn i zapisów treningowych, ale nie usunie treningów.")) },
            confirmButton = {
                TextButton(onClick = { onDeleteLocation(name); locationPendingDelete = null }) { Text(t("Delete")) }
            },
            dismissButton = { TextButton(onClick = { locationPendingDelete = null }) { Text(t("Cancel")) } },
        )
    }
}

@Composable
private fun EditLocationDialog(
    oldName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf(oldName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t("Rename location")) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(t("Location name")) }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text(t("Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}

@Composable
private fun ExportSelectionDialog(
    selection: BackupSelection,
    onSelectionChanged: (BackupSelection) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t("Export backup")) },
        text = {
            Column {
                BackupSection.entries.forEach { section ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = selection.includes(section),
                            onCheckedChange = { checked ->
                                onSelectionChanged(selection.toggled(section, checked))
                            },
                        )
                        Text(t(section.label))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = selection.hasAnySelection()) { Text(t("Choose file", "Wybierz plik")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t("Cancel")) }
        },
    )
}

private data class TrainingReportDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)

private enum class TrainingReportDateSelection {
    Start,
    End,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingReportExportDialog(
    onDismiss: () -> Unit,
    onConfirm: (TrainingReportDateRange) -> Unit,
) {
    val today = remember { LocalDate.now() }
    var startDate by remember { mutableStateOf(today.minusMonths(1)) }
    var dateSelection by remember { mutableStateOf(TrainingReportDateSelection.Start) }

    key(dateSelection) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = when (dateSelection) {
                TrainingReportDateSelection.Start -> startDate.toUtcStartOfDayMillis()
                TrainingReportDateSelection.End -> today.toUtcStartOfDayMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = pickerState.selectedDateMillis?.toUtcLocalDate() ?: return@TextButton
                        if (dateSelection == TrainingReportDateSelection.Start) {
                            startDate = selectedDate
                            dateSelection = TrainingReportDateSelection.End
                        } else {
                            onConfirm(TrainingReportDateRange(startDate, selectedDate.coerceAtLeast(startDate)))
                        }
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) {
                    Text(
                        when (dateSelection) {
                            TrainingReportDateSelection.Start -> t("Next", "Dalej")
                            TrainingReportDateSelection.End -> t("Choose file", "Wybierz plik")
                        },
                    )
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(java.time.ZoneOffset.UTC).toLocalDate()

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}
