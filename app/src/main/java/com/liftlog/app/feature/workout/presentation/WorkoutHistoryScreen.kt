package com.liftlog.app.feature.workout.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
        onWorkoutSelected = onWorkoutSelected,
    )
}

@Composable
fun WorkoutHistoryScreen(
    state: WorkoutHistoryUiState,
    onBack: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onDateFilterChanged: (String) -> Unit,
    onGymSelected: (String?) -> Unit,
    onWorkoutSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = t("Back"))
                }
                Text(t("Workout history"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
        item {
            OutlinedTextField(
                value = state.dateFilter,
                onValueChange = onDateFilterChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t("Date filter (YYYY-MM or YYYY-MM-DD)", "Filtr daty (YYYY-MM lub YYYY-MM-DD)")) },
                singleLine = true,
            )
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
                WorkoutSummaryCard(workout, onClick = { onWorkoutSelected(workout.id) })
            }
        }
    }
}

@Composable
private fun WorkoutSummaryCard(workout: WorkoutSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(workout.finishedAtEpochMillis)), fontWeight = FontWeight.SemiBold)
            Text(
                text = listOfNotNull(workout.gymLocation, t("${workout.exerciseCount} exercises", "${workout.exerciseCount} ćwiczeń"), t("${workout.volume.toInt()} kg volume", "Objętość: ${workout.volume.toInt()} kg")).joinToString(" - "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            workout.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
