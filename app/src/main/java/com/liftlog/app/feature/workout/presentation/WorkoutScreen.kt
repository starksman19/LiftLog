package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseDraft
import com.liftlog.app.core.model.LoggedExercise
import com.liftlog.app.core.model.LoggedSet
import com.liftlog.app.core.model.RecentExercisePerformance
import com.liftlog.app.core.model.WorkoutTemplate
import com.liftlog.app.core.ui.theme.LiftLogTheme
import com.liftlog.app.feature.exercises.presentation.CustomExerciseDialog
import java.text.DateFormat
import java.util.Date

@Composable
fun WorkoutRoute(
    onHistory: () -> Unit,
    onManageTemplates: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutScreen(
        state = state,
        onStartWorkout = viewModel::startWorkout,
        onSaveActiveWorkoutAsTemplate = viewModel::saveActiveWorkoutAsTemplate,
        onStartTemplate = viewModel::startTemplate,
        onAddExercises = viewModel::addExercises,
        onCreateAndAddExercise = viewModel::createAndAddExercise,
        onOpenExerciseHistory = viewModel::openExerciseHistory,
        onDismissExerciseHistory = viewModel::dismissExerciseHistory,
        onAddSet = viewModel::addSet,
        onUpdateSet = viewModel::updateSet,
        onDeleteSet = viewModel::deleteSet,
        onUpdateWorkoutDetails = viewModel::updateWorkoutDetails,
        onUpdateWorkoutExerciseNotes = viewModel::updateWorkoutExerciseNotes,
        onDeleteWorkoutExercise = viewModel::deleteWorkoutExercise,
        onFinishWorkout = viewModel::finishWorkout,
        onDiscardWorkout = viewModel::discardWorkout,
        onHistory = onHistory,
        onManageTemplates = onManageTemplates,
    )
}

@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    onStartWorkout: (String?) -> Unit,
    onSaveActiveWorkoutAsTemplate: (String) -> Unit,
    onStartTemplate: (Long, String?) -> Unit,
    onAddExercises: (List<Long>) -> Unit,
    onCreateAndAddExercise: (ExerciseDraft) -> Unit,
    onOpenExerciseHistory: (Long, String) -> Unit,
    onDismissExerciseHistory: () -> Unit,
    onAddSet: (Long, Double, Int) -> Unit,
    onUpdateSet: (Long, Double, Int) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onUpdateWorkoutDetails: (String?, String?) -> Unit,
    onUpdateWorkoutExerciseNotes: (Long, String?) -> Unit,
    onDeleteWorkoutExercise: (Long) -> Unit,
    onFinishWorkout: () -> Unit,
    onDiscardWorkout: () -> Unit,
    onHistory: () -> Unit,
    onManageTemplates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeWorkout = state.activeWorkout

    if (activeWorkout == null) {
        EmptyWorkoutScreen(
            onStartWorkout = onStartWorkout,
            templates = state.templates,
            locations = state.locations,
            onStartTemplate = onStartTemplate,
            onHistory = onHistory,
            onManageTemplates = onManageTemplates,
            modifier = modifier,
        )
    } else {
        ActiveWorkoutScreen(
            activeWorkout = activeWorkout,
            availableExercises = state.availableExercises,
            locations = state.locations,
            onSaveActiveWorkoutAsTemplate = onSaveActiveWorkoutAsTemplate,
            onAddExercises = onAddExercises,
            onCreateAndAddExercise = onCreateAndAddExercise,
            onOpenExerciseHistory = onOpenExerciseHistory,
            onAddSet = onAddSet,
            onUpdateSet = onUpdateSet,
            onDeleteSet = onDeleteSet,
            onUpdateWorkoutDetails = onUpdateWorkoutDetails,
            onUpdateWorkoutExerciseNotes = onUpdateWorkoutExerciseNotes,
            onDeleteWorkoutExercise = onDeleteWorkoutExercise,
            onFinishWorkout = onFinishWorkout,
            onDiscardWorkout = onDiscardWorkout,
            modifier = modifier,
        )
    }

    state.exerciseHistory?.let { history ->
        ExerciseHistoryDialog(
            history = history,
            onDismiss = onDismissExerciseHistory,
        )
    }
}

@Composable
private fun EmptyWorkoutScreen(
    onStartWorkout: (String?) -> Unit,
    templates: List<WorkoutTemplate>,
    locations: List<String>,
    onStartTemplate: (Long, String?) -> Unit,
    onHistory: () -> Unit,
    onManageTemplates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var startDialogVisible by remember { mutableStateOf(false) }
    var templatePendingStart by remember { mutableStateOf<WorkoutTemplate?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Workout",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onHistory) {
                Icon(Icons.Outlined.History, contentDescription = "Workout history")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { startDialogVisible = true }) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null,
            )
            Text(
                text = "Start workout",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (templates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            templates.forEach { template ->
                OutlinedButton(onClick = { templatePendingStart = template }) {
                    Icon(Icons.AutoMirrored.Outlined.PlaylistPlay, contentDescription = null)
                    Text(
                        text = "${template.name} (${template.exerciseCount})",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        OutlinedButton(onClick = onManageTemplates) {
            Icon(Icons.Outlined.BookmarkAdd, contentDescription = null)
            Text("Manage templates", modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (startDialogVisible) {
        StartWorkoutDialog(
            locations = locations,
            onDismiss = { startDialogVisible = false },
            onStart = { location ->
                onStartWorkout(location)
                startDialogVisible = false
            },
        )
    }

    templatePendingStart?.let { template ->
        StartWorkoutDialog(
            title = "Start ${template.name}",
            locations = locations,
            onDismiss = { templatePendingStart = null },
            onStart = { location ->
                onStartTemplate(template.id, location)
                templatePendingStart = null
            },
        )
    }
}

@Composable
private fun StartWorkoutDialog(
    title: String = "Start workout",
    locations: List<String>,
    onDismiss: () -> Unit,
    onStart: (String?) -> Unit,
) {
    var gymLocation by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LocationPicker(selectedLocation = gymLocation, locations = locations, onLocationSelected = { gymLocation = it })
        },
        confirmButton = {
            TextButton(onClick = { onStart(gymLocation) }) {
                Text("Start")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ActiveWorkoutScreen(
    activeWorkout: ActiveWorkout,
    availableExercises: List<Exercise>,
    locations: List<String>,
    onSaveActiveWorkoutAsTemplate: (String) -> Unit,
    onAddExercises: (List<Long>) -> Unit,
    onCreateAndAddExercise: (ExerciseDraft) -> Unit,
    onOpenExerciseHistory: (Long, String) -> Unit,
    onAddSet: (Long, Double, Int) -> Unit,
    onUpdateSet: (Long, Double, Int) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onUpdateWorkoutDetails: (String?, String?) -> Unit,
    onUpdateWorkoutExerciseNotes: (Long, String?) -> Unit,
    onDeleteWorkoutExercise: (Long) -> Unit,
    onFinishWorkout: () -> Unit,
    onDiscardWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var saveTemplateDialogVisible by remember { mutableStateOf(false) }
    var workoutDetailsDialogVisible by remember { mutableStateOf(false) }
    var exercisePickerVisible by remember { mutableStateOf(false) }
    if (exercisePickerVisible) {
        ExercisePickerScreen(
            exercises = availableExercises,
            locations = locations,
            existingExerciseIds = activeWorkout.exercises.map { it.exerciseId }.toSet(),
            onDismiss = { exercisePickerVisible = false },
            onAddExercises = { exerciseIds ->
                onAddExercises(exerciseIds)
                exercisePickerVisible = false
            },
            onCreateExercise = { draft ->
                onCreateAndAddExercise(draft)
                exercisePickerVisible = false
            },
        )
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Active Workout",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${activeWorkout.exercises.size} exercises • ${activeWorkout.exercises.sumOf { it.sets.size }} sets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                activeWorkout.gymLocation?.let { location ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = { workoutDetailsDialogVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit workout details",
                    )
                }
                IconButton(
                    onClick = { saveTemplateDialogVisible = true },
                    enabled = activeWorkout.exercises.isNotEmpty(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdd,
                        contentDescription = "Save as template",
                    )
                }
                IconButton(onClick = onDiscardWorkout) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Discard workout",
                    )
                }
                IconButton(onClick = onFinishWorkout) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Finish workout",
                    )
                }
            }
        }

        OutlinedButton(onClick = { exercisePickerVisible = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text("Add exercises", modifier = Modifier.padding(start = 8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (activeWorkout.exercises.isEmpty()) {
                item {
                    Text(
                        "This workout is empty. Add exercises when you are ready.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
            items(
                items = activeWorkout.exercises,
                key = { exercise -> exercise.id },
            ) { exercise ->
                LoggedExerciseCard(
                    exercise = exercise,
                    onAddSet = { weight, reps -> onAddSet(exercise.id, weight, reps) },
                    onUpdateSet = onUpdateSet,
                    onDeleteSet = onDeleteSet,
                    onUpdateNotes = onUpdateWorkoutExerciseNotes,
                    onDeleteExercise = onDeleteWorkoutExercise,
                    onShowHistory = onOpenExerciseHistory,
                )
            }
        }
    }

    if (saveTemplateDialogVisible) {
        SaveWorkoutTemplateDialog(
            onDismiss = { saveTemplateDialogVisible = false },
            onSave = { name ->
                onSaveActiveWorkoutAsTemplate(name)
                saveTemplateDialogVisible = false
            },
        )
    }

    if (workoutDetailsDialogVisible) {
        WorkoutDetailsDialog(
            initialGymLocation = activeWorkout.gymLocation,
            initialNotes = activeWorkout.notes,
            locations = locations,
            onDismiss = { workoutDetailsDialogVisible = false },
            onSave = { location, notes ->
                onUpdateWorkoutDetails(location, notes)
                workoutDetailsDialogVisible = false
            },
        )
    }
}

@Composable
private fun WorkoutDetailsDialog(
    initialGymLocation: String?,
    initialNotes: String?,
    locations: List<String>,
    onDismiss: () -> Unit,
    onSave: (String?, String?) -> Unit,
) {
    var gymLocation by remember { mutableStateOf(initialGymLocation) }
    var notes by remember { mutableStateOf(initialNotes.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LocationPicker(selectedLocation = gymLocation, locations = locations, onLocationSelected = { gymLocation = it })
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Workout notes") },
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(gymLocation, notes) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SaveWorkoutTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save workout template") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Template name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LocationPicker(
    selectedLocation: String?,
    locations: List<String>,
    onLocationSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Location", style = MaterialTheme.typography.labelLarge)
        FilterChip(
            selected = selectedLocation == null,
            onClick = { onLocationSelected(null) },
            label = { Text("No location") },
        )
        if (locations.isEmpty()) {
            Text("Add locations in the Locations tab.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            locations.forEach { location ->
                FilterChip(
                    selected = selectedLocation.equals(location, ignoreCase = true),
                    onClick = { onLocationSelected(location) },
                    label = { Text(location) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ExercisePickerScreen(
    exercises: List<Exercise>,
    locations: List<String>,
    existingExerciseIds: Set<Long>,
    onDismiss: () -> Unit,
    onAddExercises: (List<Long>) -> Unit,
    onCreateExercise: (ExerciseDraft) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedExerciseIds by remember { mutableStateOf(existingExerciseIds) }
    var createExerciseVisible by remember { mutableStateOf(false) }
    val matchingExercises = exercises.filter { exercise ->
        searchQuery.isBlank() || listOf(exercise.name, exercise.primaryMuscle, exercise.equipment)
            .any { it.contains(searchQuery, ignoreCase = true) }
    }
    val newSelection = selectedExerciseIds - existingExerciseIds

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Add exercises", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "Close") }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            label = { Text("Search available exercises") },
            singleLine = true,
        )
        OutlinedButton(onClick = { createExerciseVisible = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text("Create new exercise", modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(matchingExercises, key = { it.id }) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = exercise.id in selectedExerciseIds,
                            onCheckedChange = { checked ->
                                selectedExerciseIds = if (checked) selectedExerciseIds + exercise.id else selectedExerciseIds - exercise.id
                            },
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                listOfNotNull(exercise.primaryMuscle, exercise.equipment, exercise.gymLocation).joinToString(" / "),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = { onAddExercises(newSelection.toList()) },
            enabled = newSelection.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        ) {
            Text("Add ${newSelection.size} selected")
        }
    }

    if (createExerciseVisible) {
        CustomExerciseDialog(
            locations = locations,
            onDismiss = { createExerciseVisible = false },
            onSave = { draft ->
                onCreateExercise(draft)
                createExerciseVisible = false
            },
        )
    }
}

@Composable
internal fun LoggedExerciseCard(
    exercise: LoggedExercise,
    onAddSet: (Double, Int) -> Unit,
    onUpdateSet: (Long, Double, Int) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onUpdateNotes: (Long, String?) -> Unit,
    onDeleteExercise: (Long) -> Unit,
    onShowHistory: ((Long, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var editor by remember { mutableStateOf<SetEditor?>(null) }
    var setPendingDeletion by remember { mutableStateOf<LoggedSet?>(null) }
    var editNotesVisible by remember { mutableStateOf(false) }
    var exercisePendingDeletion by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${exercise.primaryMuscle} / ${exercise.equipment}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${exercise.sets.size} sets",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row {
                    IconButton(onClick = { editNotesVisible = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit exercise notes")
                    }
                    if (onShowHistory != null) {
                        IconButton(onClick = { onShowHistory(exercise.exerciseId, exercise.name) }) {
                            Icon(Icons.Outlined.Info, contentDescription = "View recent exercise results")
                        }
                    }
                    IconButton(onClick = { exercisePendingDeletion = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Remove exercise from workout")
                    }
                    OutlinedButton(
                        onClick = {
                            val previous = exercise.sets.lastOrNull()
                            editor = SetEditor(
                                setEntryId = null,
                                initialWeight = previous?.weight ?: 0.0,
                                initialReps = previous?.reps ?: 10,
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                        )
                        Text(
                            text = "Set",
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }

            exercise.notes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (exercise.sets.isEmpty()) {
                Text(
                    text = "No sets yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                exercise.sets.forEach { set ->
                    SetRow(
                        set = set,
                        onEdit = {
                            editor = SetEditor(
                                setEntryId = set.id,
                                initialWeight = set.weight,
                                initialReps = set.reps,
                            )
                        },
                        onDelete = { setPendingDeletion = set },
                    )
                }
            }
        }
    }

    editor?.let { currentEditor ->
        SetEditorDialog(
            editor = currentEditor,
            onDismiss = { editor = null },
            onSave = { weight, reps ->
                if (currentEditor.setEntryId == null) {
                    onAddSet(weight, reps)
                } else {
                    onUpdateSet(currentEditor.setEntryId, weight, reps)
                }
                editor = null
            },
        )
    }

    setPendingDeletion?.let { set ->
        AlertDialog(
            onDismissRequest = { setPendingDeletion = null },
            title = { Text("Delete set?") },
            text = { Text("Set ${set.setNumber} will be removed from this workout.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSet(set.id)
                        setPendingDeletion = null
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { setPendingDeletion = null }) { Text("Cancel") }
            },
        )
    }

    if (editNotesVisible) {
        ExerciseNotesDialog(
            initialNotes = exercise.notes,
            onDismiss = { editNotesVisible = false },
            onSave = { notes ->
                onUpdateNotes(exercise.id, notes)
                editNotesVisible = false
            },
        )
    }

    if (exercisePendingDeletion) {
        AlertDialog(
            onDismissRequest = { exercisePendingDeletion = false },
            title = { Text("Remove ${exercise.name}?") },
            text = { Text("Its sets will also be removed from this workout.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExercise(exercise.id)
                    exercisePendingDeletion = false
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { exercisePendingDeletion = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ExerciseHistoryDialog(
    history: ExerciseHistoryDialogState,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recent results: ${history.exerciseName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (history.performances.isEmpty()) {
                    Text(
                        "No completed workouts have been recorded for this exercise yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    history.performances.forEach { performance ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(performance.finishedAtEpochMillis)),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            performance.sets.forEach { set ->
                                Text(
                                    "Set ${set.setNumber}: ${set.weight.clean()} kg x ${set.reps}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun ExerciseNotesDialog(
    initialNotes: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit,
) {
    var notes by remember { mutableStateOf(initialNotes.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exercise notes") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3,
            )
        },
        confirmButton = { TextButton(onClick = { onSave(notes) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SetRow(
    set: LoggedSet,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Set ${set.setNumber}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${set.weight.clean()} kg x ${set.reps}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit set")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete set")
            }
        }
    }
}

@Composable
private fun SetEditorDialog(
    editor: SetEditor,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit,
) {
    var weight by remember(editor) { mutableStateOf(editor.initialWeight.clean()) }
    var reps by remember(editor) { mutableStateOf(editor.initialReps.toString()) }
    val parsedWeight = weight.replace(',', '.').toDoubleOrNull()
    val parsedReps = reps.toIntOrNull()
    val isValid = parsedWeight != null && parsedWeight >= 0 && parsedReps != null && parsedReps > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editor.setEntryId == null) "Add set" else "Edit set") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    isError = weight.isNotBlank() && (parsedWeight == null || parsedWeight < 0),
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reps") },
                    singleLine = true,
                    isError = reps.isNotBlank() && (parsedReps == null || parsedReps <= 0),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(parsedWeight ?: 0.0, parsedReps ?: 1) },
                enabled = isValid,
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private data class SetEditor(
    val setEntryId: Long?,
    val initialWeight: Double,
    val initialReps: Int,
)

private fun Double.clean(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

@Preview
@Composable
private fun WorkoutScreenPreview() {
    LiftLogTheme {
        WorkoutScreen(
            state = WorkoutUiState(
                activeWorkout = ActiveWorkout(
                    id = 1,
                    startedAtEpochMillis = 0,
                    gymLocation = null,
                    notes = null,
                    exercises = listOf(
                        LoggedExercise(
                            id = 1,
                            exerciseId = 1,
                            name = "Bench Press",
                            primaryMuscle = "Chest",
                            equipment = "Barbell",
                            orderIndex = 0,
                            notes = "Keep the bar path controlled.",
                            sets = listOf(
                                LoggedSet(id = 1, setNumber = 1, weight = 80.0, reps = 8),
                                LoggedSet(id = 2, setNumber = 2, weight = 80.0, reps = 8),
                            ),
                        ),
                    ),
                ),
                availableExercises = listOf(
                    Exercise(1, "Bench Press", "Chest", "Barbell", com.liftlog.app.core.model.ExerciseCategory.FreeWeights, null, null, null, false),
                    Exercise(2, "Squat", "Legs", "Barbell", com.liftlog.app.core.model.ExerciseCategory.FreeWeights, null, null, null, false),
                ),
            ),
            onStartWorkout = {},
            onSaveActiveWorkoutAsTemplate = {},
            onStartTemplate = { _, _ -> },
            onAddExercises = {},
            onCreateAndAddExercise = {},
            onOpenExerciseHistory = { _, _ -> },
            onDismissExerciseHistory = {},
            onAddSet = { _, _, _ -> },
            onUpdateSet = { _, _, _ -> },
            onDeleteSet = {},
            onUpdateWorkoutDetails = { _, _ -> },
            onUpdateWorkoutExerciseNotes = { _, _ -> },
            onDeleteWorkoutExercise = {},
            onFinishWorkout = {},
            onDiscardWorkout = {},
            onHistory = {},
            onManageTemplates = {},
        )
    }
}
