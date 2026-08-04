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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.feature.backup.domain.BackupSection
import com.liftlog.app.feature.backup.domain.BackupSelection

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onWeightUnitChanged = viewModel::setWeightUnit,
        onDefaultRestChanged = viewModel::setDefaultRestSeconds,
        onExport = viewModel::exportTo,
        onInspectImport = viewModel::inspectImport,
        onImport = viewModel::importFrom,
        onDismissImportPreview = viewModel::dismissImportPreview,
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onWeightUnitChanged: (WeightUnit) -> Unit,
    onDefaultRestChanged: (Int) -> Unit,
    onExport: (android.net.Uri, BackupSelection) -> Unit,
    onInspectImport: (android.net.Uri) -> Unit,
    onImport: (android.net.Uri) -> Unit,
    onDismissImportPreview: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var exportDialogVisible by remember { mutableStateOf(false) }
    var exportSelection by remember { mutableStateOf(BackupSelection.Everything) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { onExport(it, exportSelection) } },
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(onInspectImport) },
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
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                SettingsSection(title = "Units") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        WeightUnit.entries.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = state.settings.weightUnit == unit,
                                onClick = { onWeightUnitChanged(unit) },
                                shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                                label = { Text(if (unit == WeightUnit.Kilograms) "Kilograms" else "Pounds") },
                            )
                        }
                    }
                }
            }
            item {
                SettingsSection(title = "Default rest") {
                    Text(
                        text = "${state.settings.defaultRestSeconds} seconds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Slider(
                        value = state.settings.defaultRestSeconds.toFloat(),
                        onValueChange = { onDefaultRestChanged(it.toInt()) },
                        valueRange = 0f..300f,
                    )
                }
            }
            item {
                SettingsSection(title = "Data transfer") {
                    OutlinedButton(
                        onClick = { exportDialogVisible = true },
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.FileDownload, contentDescription = null)
                        Text("Export backup", modifier = Modifier.padding(start = 8.dp))
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        enabled = !state.isWorking,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.FileUpload, contentDescription = null)
                        Text("Import backup", modifier = Modifier.padding(start = 8.dp))
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

    state.importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = onDismissImportPreview,
            title = { Text("Import backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The file contains:")
                    BackupSection.entries
                        .filter(preview.contents.selection::includes)
                        .forEach { section -> Text(section.label) }
                    Text(
                        text = "${preview.contents.summary.exercises} exercises, ${preview.contents.summary.workouts} workouts, ${preview.contents.summary.sets} sets, ${preview.contents.summary.templates} templates",
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
                ) { Text("Import selected data") }
            },
            dismissButton = {
                TextButton(onClick = onDismissImportPreview) { Text("Cancel") }
            },
        )
    }
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
        title = { Text("Export backup") },
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
                        Text(section.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = selection.hasAnySelection()) { Text("Choose file") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

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
