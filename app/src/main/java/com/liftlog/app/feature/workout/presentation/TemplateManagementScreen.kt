package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.liftlog.app.core.model.WorkoutPlan
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
        onSavePlan = viewModel::savePlan,
        onDeletePlan = viewModel::deletePlan,
    )
}

@Composable
fun TemplateManagementScreen(
    state: TemplateManagementUiState,
    onBack: () -> Unit,
    onLoadExerciseIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onSave: (Long?, String, Set<Long>, Long?) -> Unit,
    onDelete: (Long) -> Unit,
    onSavePlan: (Long?, String) -> Unit,
    onDeletePlan: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var createDialogVisible by remember { mutableStateOf(false) }
    var templatePendingDeletion by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var editedPlan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var createPlanDialogVisible by remember { mutableStateOf(false) }
    var planPendingDeletion by remember { mutableStateOf<WorkoutPlan?>(null) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { createDialogVisible = true }) {
                Icon(Icons.Outlined.Add, t("Create template", "Utworz szablon"))
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
                    Text(t("Workout templates", "Szablony treningow"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { createPlanDialogVisible = true }) {
                        Icon(Icons.Outlined.CreateNewFolder, t("Create training plan", "Utworz plan treningowy"))
                    }
                }
            }
            item {
                Text(t("Training plans", "Plany treningowe"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (state.plans.isEmpty()) {
                item { Text(t("No training plans yet.", "Brak planow treningowych."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.plans, key = { it.id }) { plan ->
                    val count = state.templates.count { it.planId == plan.id }
                    PlanRow(plan, count, onEdit = { editedPlan = plan }, onDelete = { planPendingDeletion = plan })
                }
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp)) }
            item {
                Text(t("Templates", "Szablony"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (state.templates.isEmpty()) {
                item { Text(t("Create a template to start a saved exercise plan in one step.", "Utworz szablon, aby zaczynac zapisany trening jednym krokiem.")) }
            } else {
                items(state.templates, key = { it.id }) { template ->
                    TemplateRow(template, onEdit = { editedTemplate = template }, onDelete = { templatePendingDeletion = template })
                }
            }
        }
    }
    if (createDialogVisible) {
        TemplateEditorDialog(
            title = t("New template", "Nowy szablon"), initialName = "", initialExerciseIds = emptySet(), initialPlanId = null,
            exercises = state.exercises, plans = state.plans, onDismiss = { createDialogVisible = false },
            onSave = { name, ids, planId -> onSave(null, name, ids, planId); createDialogVisible = false },
        )
    }
    editedTemplate?.let { template ->
        TemplateEditorLoader(template, state.exercises, state.plans, onLoadExerciseIds, { editedTemplate = null }) { name, ids, planId ->
            onSave(template.id, name, ids, planId)
            editedTemplate = null
        }
    }
    templatePendingDeletion?.let { template ->
        AlertDialog(
            onDismissRequest = { templatePendingDeletion = null },
            title = { Text(t("Delete ${template.name}?", "Usunac ${template.name}?")) },
            confirmButton = { TextButton(onClick = { onDelete(template.id); templatePendingDeletion = null }) { Text(t("Delete")) } },
            dismissButton = { TextButton(onClick = { templatePendingDeletion = null }) { Text(t("Cancel")) } },
        )
    }
    if (createPlanDialogVisible) {
        PlanEditorDialog(t("New training plan", "Nowy plan treningowy"), "", { createPlanDialogVisible = false }) { name ->
            onSavePlan(null, name)
            createPlanDialogVisible = false
        }
    }
    editedPlan?.let { plan ->
        PlanEditorDialog(t("Edit training plan", "Edytuj plan treningowy"), plan.name, { editedPlan = null }) { name ->
            onSavePlan(plan.id, name)
            editedPlan = null
        }
    }
    planPendingDeletion?.let { plan ->
        AlertDialog(
            onDismissRequest = { planPendingDeletion = null },
            title = { Text(t("Delete ${plan.name}?", "Usunac ${plan.name}?")) },
            text = { Text(t("Its templates will remain available without a plan.", "Szablony pozostana dostepne jako nieprzypisane.")) },
            confirmButton = { TextButton(onClick = { onDeletePlan(plan.id); planPendingDeletion = null }) { Text(t("Delete")) } },
            dismissButton = { TextButton(onClick = { planPendingDeletion = null }) { Text(t("Cancel")) } },
        )
    }
}

@Composable
private fun PlanRow(plan: WorkoutPlan, templateCount: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(plan.name, fontWeight = FontWeight.SemiBold)
            Text(t("$templateCount templates", "$templateCount szablonow"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row { IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, t("Edit")) }; IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, t("Delete")) } }
    }
}

@Composable
private fun TemplateRow(template: WorkoutTemplate, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(template.name, fontWeight = FontWeight.SemiBold)
            val placement = template.planName ?: t("Ungrouped", "Bez grupy")
            Text(t("${template.exerciseCount} exercises - $placement", "${template.exerciseCount} cwiczen - $placement"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row { IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, t("Edit template", "Edytuj szablon")) }; IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, t("Delete template", "Usun szablon")) } }
    }
}

@Composable
private fun TemplateEditorLoader(
    template: WorkoutTemplate, exercises: List<Exercise>, plans: List<WorkoutPlan>,
    onLoadExerciseIds: (Long, (Set<Long>) -> Unit) -> Unit, onDismiss: () -> Unit,
    onSave: (String, Set<Long>, Long?) -> Unit,
) {
    var ids by remember { mutableStateOf<Set<Long>?>(null) }
    androidx.compose.runtime.LaunchedEffect(template.id) { onLoadExerciseIds(template.id) { ids = it } }
    ids?.let { selectedIds -> TemplateEditorDialog(t("Edit template", "Edytuj szablon"), template.name, selectedIds, template.planId, exercises, plans, onDismiss, onSave) }
}

@Composable
private fun TemplateEditorDialog(
    title: String, initialName: String, initialExerciseIds: Set<Long>, initialPlanId: Long?, exercises: List<Exercise>, plans: List<WorkoutPlan>,
    onDismiss: () -> Unit, onSave: (String, Set<Long>, Long?) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIds by remember { mutableStateOf(initialExerciseIds) }
    var planId by remember { mutableStateOf(initialPlanId) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(t("Template name", "Nazwa szablonu")) }, singleLine = true)
                Text(t("Training plan", "Plan treningowy"), style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = planId == null, onCheckedChange = { if (it) planId = null })
                    Text(t("Ungrouped", "Bez grupy"))
                }
                plans.forEach { plan ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = planId == plan.id, onCheckedChange = { if (it) planId = plan.id })
                        Text(plan.name)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(t("Exercises", "Cwiczenia"), style = MaterialTheme.typography.labelLarge)
                exercises.forEach { exercise ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = exercise.id in selectedIds, onCheckedChange = { checked -> selectedIds = if (checked) selectedIds + exercise.id else selectedIds - exercise.id })
                        Text(exercise.name)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, selectedIds, planId) }, enabled = name.isNotBlank() && selectedIds.isNotEmpty()) { Text(t("Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}

@Composable
private fun PlanEditorDialog(title: String, initialName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(t("Plan name", "Nazwa planu")) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) { Text(t("Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}
