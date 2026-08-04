package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.WorkoutDetail
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutDetailRoute(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: WorkoutDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState(workoutId).collectAsStateWithLifecycle()
    WorkoutDetailScreen(
        state = state,
        onBack = onBack,
        onUpdateWorkout = viewModel::updateWorkout,
        onDeleteWorkout = { viewModel.deleteWorkout(workoutId); onBack() },
        onAddExercise = { exerciseId -> viewModel.addExercise(workoutId, exerciseId) },
        onAddSet = viewModel::addSet,
        onUpdateSet = viewModel::updateSet,
        onDeleteSet = viewModel::deleteSet,
        onUpdateExerciseNotes = viewModel::updateExerciseNotes,
        onDeleteExercise = viewModel::deleteExercise,
    )
}

@Composable
fun WorkoutDetailScreen(
    state: WorkoutDetailUiState,
    onBack: () -> Unit,
    onUpdateWorkout: (WorkoutDetail, Long, Long, String?, String?) -> Unit,
    onDeleteWorkout: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onAddSet: (Long, Double, Int) -> Unit,
    onUpdateSet: (Long, Double, Int) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onUpdateExerciseNotes: (Long, String?) -> Unit,
    onDeleteExercise: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val workout = state.workout
    if (workout == null) {
        Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
            Text("Workout not found")
        }
        return
    }
    var editDetails by remember { mutableStateOf(false) }
    var deleteConfirmation by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                    Text("Workout details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Row {
                    IconButton(onClick = { editDetails = true }) { Icon(Icons.Outlined.Edit, "Edit workout") }
                    IconButton(onClick = { deleteConfirmation = true }) { Icon(Icons.Outlined.Delete, "Delete workout") }
                }
            }
        }
        item {
            Text(
                text = "${workout.gymLocation ?: "No location"} · ${workout.exercises.size} exercises",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            workout.notes?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.availableExercises, key = { it.id }) { exercise ->
                    AssistChip(
                        onClick = { onAddExercise(exercise.id) },
                        label = { Text(exercise.name) },
                        leadingIcon = { Icon(Icons.Outlined.Add, null) },
                    )
                }
            }
        }
        items(workout.exercises, key = { it.id }) { exercise ->
            LoggedExerciseCard(
                exercise = exercise,
                onAddSet = { weight, reps -> onAddSet(exercise.id, weight, reps) },
                onUpdateSet = onUpdateSet,
                onDeleteSet = onDeleteSet,
                onUpdateNotes = onUpdateExerciseNotes,
                onDeleteExercise = onDeleteExercise,
            )
        }
    }
    if (editDetails) {
        CompletedWorkoutDetailsDialog(
            workout = workout,
            onDismiss = { editDetails = false },
            onSave = { startedAt, finishedAt, gym, notes ->
                onUpdateWorkout(workout, startedAt, finishedAt, gym, notes)
                editDetails = false
            },
        )
    }
    if (deleteConfirmation) {
        AlertDialog(
            onDismissRequest = { deleteConfirmation = false },
            title = { Text("Delete this workout?") },
            text = { Text("All exercises and sets in this session will be removed.") },
            confirmButton = { TextButton(onClick = onDeleteWorkout) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteConfirmation = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun CompletedWorkoutDetailsDialog(
    workout: WorkoutDetail,
    onDismiss: () -> Unit,
    onSave: (Long, Long, String?, String?) -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    var date by remember { mutableStateOf(workout.finishedAtEpochMillis.toDateText(formatter)) }
    var gym by remember { mutableStateOf(workout.gymLocation.orEmpty()) }
    var notes by remember { mutableStateOf(workout.notes.orEmpty()) }
    val updatedTimestamp = date.toEpochAtSameTime(workout.finishedAtEpochMillis, formatter)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true)
                OutlinedTextField(value = gym, onValueChange = { gym = it }, label = { Text("Gym / location") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Workout notes") }, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(updatedTimestamp, updatedTimestamp, gym, notes) }, enabled = updatedTimestamp > 0) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun Long.toDateText(formatter: DateTimeFormatter): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)

private fun String.toEpochAtSameTime(originalEpochMillis: Long, formatter: DateTimeFormatter): Long = runCatching {
    val original = Instant.ofEpochMilli(originalEpochMillis).atZone(ZoneId.systemDefault())
    LocalDate.parse(this, formatter).atTime(original.toLocalTime()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}.getOrDefault(-1)
