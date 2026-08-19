package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.WorkoutSummary
import com.liftlog.app.core.ui.localization.t
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

@Composable
fun WorkoutHistoryRoute(
    onBack: () -> Unit,
    onWorkoutSelected: (Long) -> Unit,
    viewModel: WorkoutHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WorkoutHistoryScreen(
        state = state,
        onBack = onBack,
        onSearchChanged = viewModel::updateSearch,
        onDateFilterChanged = viewModel::updateDateFilter,
        onGymSelected = viewModel::selectGym,
        onSortModeChanged = viewModel::updateSortMode,
        onWorkoutSelected = onWorkoutSelected,
        onDeleteWorkout = viewModel::deleteWorkout,
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    state: WorkoutHistoryUiState,
    onBack: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onDateFilterChanged: (String) -> Unit,
    onGymSelected: (String?) -> Unit,
    onSortModeChanged: (WorkoutSortMode) -> Unit,
    onWorkoutSelected: (Long) -> Unit,
    onDeleteWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var workoutPendingDelete by remember { mutableStateOf<WorkoutSummary?>(null) }
    var sortMenuVisible by remember { mutableStateOf(false) }
    var datePickerVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.sortMode) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = t("Back"))
                    }
                    Text(t("Workout history"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Box {
                    IconButton(onClick = { sortMenuVisible = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = t("Sort", "Sortuj"))
                    }
                    DropdownMenu(expanded = sortMenuVisible, onDismissRequest = { sortMenuVisible = false }) {
                        WorkoutSortMode.entries.forEach { sortMode ->
                            DropdownMenuItem(
                                text = { Text(sortMode.label()) },
                                onClick = {
                                    onSortModeChanged(sortMode)
                                    sortMenuVisible = false
                                },
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { datePickerVisible = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.DateRange, contentDescription = null)
                    Text(
                        text = state.dateFilter.takeIf { it.isNotBlank() } ?: t("Choose date", "Wybierz datę"),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (state.dateFilter.isNotBlank()) {
                    IconButton(onClick = { onDateFilterChanged("") }) {
                        Icon(Icons.Outlined.Delete, contentDescription = t("Clear date filter", "Wyczyść filtr daty"))
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t("Search location or notes", "Szukaj lokalizacji lub notatek")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
            )
        }
        if (state.gyms.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { AssistChip(onClick = { onGymSelected(null) }, label = { Text(t("All gyms", "Wszystkie siłownie")) }) }
                    items(state.gyms) { gym ->
                        AssistChip(onClick = { onGymSelected(gym) }, label = { Text(gym) })
                    }
                }
            }
        }
        if (state.workouts.isEmpty()) {
            item { Text(t("No completed workouts match these filters."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.workouts, key = { it.id }) { workout ->
                WorkoutSummaryCard(
                    workout = workout,
                    onClick = { onWorkoutSelected(workout.id) },
                    onDelete = { workoutPendingDelete = workout },
                )
            }
        }
    }

    if (datePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateFilter.toLocalDateOrNull()?.toUtcStartOfDayMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            onDateFilterChanged(selected.toUtcLocalDate().toString())
                        }
                        datePickerVisible = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text(t("Apply", "Zastosuj")) }
            },
            dismissButton = { TextButton(onClick = { datePickerVisible = false }) { Text(t("Cancel")) } },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    workoutPendingDelete?.let { workout ->
        AlertDialog(
            onDismissRequest = { workoutPendingDelete = null },
            title = { Text(t("Delete this workout?", "Usunąć ten trening?")) },
            text = { Text(t("All exercises and sets in this session will be removed.", "Wszystkie ćwiczenia i serie z tego treningu zostaną usunięte.")) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteWorkout(workout.id)
                    workoutPendingDelete = null
                }) { Text(t("Delete")) }
            },
            dismissButton = { TextButton(onClick = { workoutPendingDelete = null }) { Text(t("Cancel")) } },
        )
    }
}

@Composable
private fun WorkoutSummaryCard(workout: WorkoutSummary, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(workout.finishedAtEpochMillis)), fontWeight = FontWeight.SemiBold)
                Text(
                    text = listOfNotNull(workout.gymLocation, t("${workout.exerciseCount} exercises", "${workout.exerciseCount} ćwiczeń")).joinToString(" - "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                workout.exerciseNames.takeIf { it.isNotBlank() }?.let { names ->
                    Text(names.split(',').joinToString(separator = "\n") { it.trim() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                workout.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = t("Delete workout", "Usuń trening"))
            }
        }
    }
}

@Composable
private fun WorkoutSortMode.label(): String = when (this) {
    WorkoutSortMode.NewestFirst -> t("Newest first", "Najnowsze najpierw")
    WorkoutSortMode.OldestFirst -> t("Oldest first", "Najstarsze najpierw")
    WorkoutSortMode.Location -> t("Location", "Lokalizacja")
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
