package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.core.ui.localization.t

@Composable
fun TemplateManagementRoute(
    onBack: () -> Unit,
    viewModel: TemplateManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TemplateManagementScreen(
        state = state,
        onBack = onBack,
        onLoadExerciseIds = viewModel::loadExerciseIds,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
    )
}

@Composable
fun TemplateManagementScreen(
    state: TemplateManagementUiState,
    onBack: () -> Unit,
    onLoadExerciseIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onSave: (Long?, String, Set<Long>) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var createDialogVisible by remember { mutableStateOf(false) }
    var templatePendingDeletion by remember { mutableStateOf<WorkoutTemplate?>(null) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { createDialogVisible = true }) {
                Icon(Icons.Outlined.Add, t("Create template", "Utwórz szablon"))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = innerPadding.calculateTopPadding() + 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, t("Back")) }
                    Text(t("Workout templates", "Szablony treningów"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
            if (state.templates.isEmpty()) {
                item { Text(t("Create a template to start a saved exercise plan in one step.", "Utwórz szablon, aby rozpocząć zapisany plan ćwiczeń jednym krokiem.")) }
            } else {
                items(state.templates, key = { it.id }) { template ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(template.name, fontWeight = FontWeight.SemiBold)
                            Text(t("${template.exerciseCount} exercises", "${template.exerciseCount} ćwiczeń"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            IconButton(onClick = { editedTemplate = template }) { Icon(Icons.Outlined.Edit, t("Edit template", "Edytuj szablon")) }
                            IconButton(onClick = { templatePendingDeletion = template }) { Icon(Icons.Outlined.Delete, t("Delete template", "Usuń szablon")) }
                        }
                    }
                }
            }
        }
    }
    if (createDialogVisible) {
        TemplateEditorDialog(
            title = t("New template", "Nowy szablon"),
            initialName = "",
            initialExerciseIds = emptySet(),
            exercises = state.exercises,
            onDismiss = { createDialogVisible = false },
            onSave = { name, ids -> onSave(null, name, ids); createDialogVisible = false },
        )
    }
    editedTemplate?.let { template ->
        TemplateEditorLoader(
            template = template,
            exercises = state.exercises,
            onLoadExerciseIds = onLoadExerciseIds,
            onDismiss = { editedTemplate = null },
            onSave = { name, ids -> onSave(template.id, name, ids); editedTemplate = null },
        )
    }
    templatePendingDeletion?.let { template ->
        AlertDialog(
            onDismissRequest = { templatePendingDeletion = null },
            title = { Text(t("Delete ${template.name}?", "Usunąć ${template.name}?")) },
            confirmButton = { TextButton(onClick = { onDelete(template.id); templatePendingDeletion = null }) { Text(t("Delete")) } },
            dismissButton = { TextButton(onClick = { templatePendingDeletion = null }) { Text(t("Cancel")) } },
        )
    }
}

@Composable
private fun TemplateEditorLoader(
    template: WorkoutTemplate,
    exercises: List<Exercise>,
    onLoadExerciseIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Set<Long>) -> Unit,
) {
    var ids by remember { mutableStateOf<Set<Long>?>(null) }
    androidx.compose.runtime.LaunchedEffect(template.id) { onLoadExerciseIds(template.id) { ids = it } }
    ids?.let { selectedIds ->
        TemplateEditorDialog(
            title = t("Edit template", "Edytuj szablon"),
            initialName = template.name,
            initialExerciseIds = selectedIds,
            exercises = exercises,
            onDismiss = onDismiss,
            onSave = onSave,
        )
    }
}

@Composable
private fun TemplateEditorDialog(
    title: String,
    initialName: String,
    initialExerciseIds: Set<Long>,
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (String, Set<Long>) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIds by remember { mutableStateOf(initialExerciseIds) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(t("Template name", "Nazwa szablonu")) }, singleLine = true)
                Text(t("Exercises"), style = MaterialTheme.typography.labelLarge)
                exercises.forEach { exercise ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = exercise.id in selectedIds,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) selectedIds + exercise.id else selectedIds - exercise.id
                            },
                        )
                        Text(exercise.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, selectedIds) }, enabled = name.isNotBlank() && selectedIds.isNotEmpty()) { Text(t("Save")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}
