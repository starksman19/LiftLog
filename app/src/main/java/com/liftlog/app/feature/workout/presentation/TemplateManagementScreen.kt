package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
        onLoadTemplatePlanIds = viewModel::loadTemplatePlanIds,
        onLoadPlanTemplateIds = viewModel::loadPlanTemplateIds,
        onSaveTemplate = viewModel::save,
        onDeleteTemplate = viewModel::delete,
        onSavePlan = viewModel::savePlan,
        onDeletePlan = viewModel::deletePlan,
    )
}

@Composable
fun TemplateManagementScreen(
    state: TemplateManagementUiState,
    onBack: () -> Unit,
    onLoadExerciseIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onLoadTemplatePlanIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onLoadPlanTemplateIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onSaveTemplate: (Long?, String, Set<Long>, Set<Long>) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
    onSavePlan: (Long?, String, Set<Long>) -> Unit,
    onDeletePlan: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var createTemplateVisible by remember { mutableStateOf(false) }
    var templatePendingDeletion by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var editedPlan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var createPlanVisible by remember { mutableStateOf(false) }
    var planPendingDeletion by remember { mutableStateOf<WorkoutPlan?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    onClick = { createPlanVisible = true },
                    icon = { Icon(Icons.Outlined.CreateNewFolder, null) },
                    text = { Text(t("Add training plan", "Dodaj plan treningowy")) },
                )
                ExtendedFloatingActionButton(
                    onClick = { createTemplateVisible = true },
                    icon = { Icon(Icons.Outlined.Add, null) },
                    text = { Text(t("Add template", "Dodaj szablon")) },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = innerPadding.calculateTopPadding() + 20.dp, end = 20.dp, bottom = 176.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, t("Back")) }
                    Text(t("Manage templates and plans", "Zarzadzaj szablonami i planami"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
            item { Text(t("Training plans", "Plany treningowe"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            if (state.plans.isEmpty()) {
                item { Text(t("No training plans yet.", "Brak planow treningowych."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.plans, key = { it.id }) { plan ->
                    PlanRow(plan, state.templates.count { plan.id in it.planIds }, onEdit = { editedPlan = plan }, onDelete = { planPendingDeletion = plan })
                }
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp)) }
            item { Text(t("Templates", "Szablony"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            if (state.templates.isEmpty()) {
                item { Text(t("Add a template to reuse an exercise list.", "Dodaj szablon, aby ponownie wykorzystywac liste cwiczen.")) }
            } else {
                items(state.templates, key = { it.id }) { template ->
                    TemplateRow(template, onEdit = { editedTemplate = template }, onDelete = { templatePendingDeletion = template })
                }
            }
        }
    }

    if (createTemplateVisible) {
        TemplateEditorDialog(
            title = t("New template", "Nowy szablon"), initialName = "", initialExerciseIds = emptySet(), initialPlanIds = emptySet(),
            exercises = state.exercises, plans = state.plans, onDismiss = { createTemplateVisible = false },
            onSave = { name, exerciseIds, planIds -> onSaveTemplate(null, name, exerciseIds, planIds); createTemplateVisible = false },
        )
    }
    editedTemplate?.let { template ->
        TemplateEditorLoader(template, state.exercises, state.plans, onLoadExerciseIds, onLoadTemplatePlanIds, { editedTemplate = null }) { name, exerciseIds, planIds ->
            onSaveTemplate(template.id, name, exerciseIds, planIds)
            editedTemplate = null
        }
    }
    templatePendingDeletion?.let { template ->
        AlertDialog(
            onDismissRequest = { templatePendingDeletion = null },
            title = { Text(t("Delete ${template.name}?", "Usunac ${template.name}?")) },
            confirmButton = { TextButton(onClick = { onDeleteTemplate(template.id); templatePendingDeletion = null }) { Text(t("Delete")) } },
            dismissButton = { TextButton(onClick = { templatePendingDeletion = null }) { Text(t("Cancel")) } },
        )
    }
    if (createPlanVisible) {
        PlanEditorDialog(t("New training plan", "Nowy plan treningowy"), "", emptySet(), state.templates, { createPlanVisible = false }) { name, templateIds ->
            onSavePlan(null, name, templateIds)
            createPlanVisible = false
        }
    }
    editedPlan?.let { plan ->
        PlanEditorLoader(plan, state.templates, onLoadPlanTemplateIds, { editedPlan = null }) { name, templateIds ->
            onSavePlan(plan.id, name, templateIds)
            editedPlan = null
        }
    }
    planPendingDeletion?.let { plan ->
        AlertDialog(
            onDismissRequest = { planPendingDeletion = null },
            title = { Text(t("Delete ${plan.name}?", "Usunac ${plan.name}?")) },
            text = { Text(t("Its templates will remain available and can belong to other plans.", "Szablony pozostana dostepne i moga nalezec do innych planow.")) },
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
            val placement = template.planNames.takeIf { it.isNotEmpty() }?.joinToString() ?: t("Ungrouped", "Bez grupy")
            Text(t("${template.exerciseCount} exercises - $placement", "${template.exerciseCount} cwiczen - $placement"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row { IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, t("Edit template", "Edytuj szablon")) }; IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, t("Delete template", "Usun szablon")) } }
    }
}

@Composable
private fun TemplateEditorLoader(
    template: WorkoutTemplate, exercises: List<Exercise>, plans: List<WorkoutPlan>,
    onLoadExerciseIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onLoadPlanIds: (Long, (Set<Long>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Set<Long>, Set<Long>) -> Unit,
) {
    var exerciseIds by remember { mutableStateOf<Set<Long>?>(null) }
    var planIds by remember { mutableStateOf<Set<Long>?>(null) }
    LaunchedEffect(template.id) {
        onLoadExerciseIds(template.id) { exerciseIds = it }
        onLoadPlanIds(template.id) { planIds = it }
    }
    if (exerciseIds != null && planIds != null) {
        TemplateEditorDialog(t("Edit template", "Edytuj szablon"), template.name, exerciseIds!!, planIds!!, exercises, plans, onDismiss, onSave)
    }
}

@Composable
private fun TemplateEditorDialog(
    title: String, initialName: String, initialExerciseIds: Set<Long>, initialPlanIds: Set<Long>, exercises: List<Exercise>, plans: List<WorkoutPlan>,
    onDismiss: () -> Unit, onSave: (String, Set<Long>, Set<Long>) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedExerciseIds by remember { mutableStateOf(initialExerciseIds) }
    var selectedPlanIds by remember { mutableStateOf(initialPlanIds) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(t("Template name", "Nazwa szablonu")) }, singleLine = true)
                Text(t("Training plans", "Plany treningowe"), style = MaterialTheme.typography.labelLarge)
                if (plans.isEmpty()) Text(t("Create a plan later to group this template.", "Utworz plan pozniej, aby pogrupowac ten szablon."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                plans.forEach { plan -> SelectionRow(plan.name, plan.id in selectedPlanIds) { checked -> selectedPlanIds = if (checked) selectedPlanIds + plan.id else selectedPlanIds - plan.id } }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(t("Exercises", "Cwiczenia"), style = MaterialTheme.typography.labelLarge)
                exercises.forEach { exercise -> SelectionRow(exercise.name, exercise.id in selectedExerciseIds) { checked -> selectedExerciseIds = if (checked) selectedExerciseIds + exercise.id else selectedExerciseIds - exercise.id } }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name.trim(), selectedExerciseIds, selectedPlanIds) }, enabled = name.isNotBlank() && selectedExerciseIds.isNotEmpty()) { Text(t("Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}

@Composable
private fun PlanEditorLoader(plan: WorkoutPlan, templates: List<WorkoutTemplate>, onLoadTemplateIds: (Long, (Set<Long>) -> Unit) -> Unit, onDismiss: () -> Unit, onSave: (String, Set<Long>) -> Unit) {
    var templateIds by remember { mutableStateOf<Set<Long>?>(null) }
    LaunchedEffect(plan.id) { onLoadTemplateIds(plan.id) { templateIds = it } }
    templateIds?.let { PlanEditorDialog(t("Edit training plan", "Edytuj plan treningowy"), plan.name, it, templates, onDismiss, onSave) }
}

@Composable
private fun PlanEditorDialog(title: String, initialName: String, initialTemplateIds: Set<Long>, templates: List<WorkoutTemplate>, onDismiss: () -> Unit, onSave: (String, Set<Long>) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var selectedTemplateIds by remember { mutableStateOf(initialTemplateIds) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(t("Plan name", "Nazwa planu")) }, singleLine = true)
                Text(t("Templates in this plan", "Szablony w tym planie"), style = MaterialTheme.typography.labelLarge)
                if (templates.isEmpty()) Text(t("Create templates first.", "Najpierw utworz szablony."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                templates.forEach { template -> SelectionRow(template.name, template.id in selectedTemplateIds) { checked -> selectedTemplateIds = if (checked) selectedTemplateIds + template.id else selectedTemplateIds - template.id } }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name.trim(), selectedTemplateIds) }, enabled = name.isNotBlank()) { Text(t("Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}

@Composable
private fun SelectionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
