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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.ActiveWorkout
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.LoggedExercise
import com.liftlog.app.core.model.LoggedSet
import com.liftlog.app.core.ui.theme.LiftLogTheme

@Composable
fun WorkoutRoute(
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutScreen(
        state = state,
        onStartWorkout = viewModel::startWorkout,
        onAddExercise = viewModel::addExercise,
        onAddQuickSet = viewModel::addQuickSet,
        onFinishWorkout = viewModel::finishWorkout,
        onDiscardWorkout = viewModel::discardWorkout,
    )
}

@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    onStartWorkout: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onAddQuickSet: (Long) -> Unit,
    onFinishWorkout: () -> Unit,
    onDiscardWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeWorkout = state.activeWorkout

    if (activeWorkout == null) {
        EmptyWorkoutScreen(
            onStartWorkout = onStartWorkout,
            modifier = modifier,
        )
    } else {
        ActiveWorkoutScreen(
            activeWorkout = activeWorkout,
            availableExercises = state.availableExercises,
            onAddExercise = onAddExercise,
            onAddQuickSet = onAddQuickSet,
            onFinishWorkout = onFinishWorkout,
            onDiscardWorkout = onDiscardWorkout,
            modifier = modifier,
        )
    }
}

@Composable
private fun EmptyWorkoutScreen(
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Workout",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartWorkout) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null,
            )
            Text(
                text = "Start workout",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun ActiveWorkoutScreen(
    activeWorkout: ActiveWorkout,
    availableExercises: List<Exercise>,
    onAddExercise: (Long) -> Unit,
    onAddQuickSet: (Long) -> Unit,
    onFinishWorkout: () -> Unit,
    onDiscardWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    text = "${activeWorkout.exercises.size} exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row {
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 20.dp),
        ) {
            items(
                items = availableExercises,
                key = { exercise -> exercise.id },
            ) { exercise ->
                AssistChip(
                    onClick = { onAddExercise(exercise.id) },
                    label = { Text(exercise.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                        )
                    },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = activeWorkout.exercises,
                key = { exercise -> exercise.id },
            ) { exercise ->
                LoggedExerciseCard(
                    exercise = exercise,
                    onAddQuickSet = { onAddQuickSet(exercise.id) },
                )
            }
        }
    }
}

@Composable
private fun LoggedExerciseCard(
    exercise: LoggedExercise,
    onAddQuickSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                }

                OutlinedButton(onClick = onAddQuickSet) {
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

            if (exercise.sets.isEmpty()) {
                Text(
                    text = "No sets yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                exercise.sets.forEach { set ->
                    SetRow(set = set)
                }
            }
        }
    }
}

@Composable
private fun SetRow(set: LoggedSet) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Set ${set.setNumber}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "${set.weight.clean()} kg x ${set.reps}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

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
                    exercises = listOf(
                        LoggedExercise(
                            id = 1,
                            exerciseId = 1,
                            name = "Bench Press",
                            primaryMuscle = "Chest",
                            equipment = "Barbell",
                            orderIndex = 0,
                            sets = listOf(
                                LoggedSet(id = 1, setNumber = 1, weight = 80.0, reps = 8),
                                LoggedSet(id = 2, setNumber = 2, weight = 80.0, reps = 8),
                            ),
                        ),
                    ),
                ),
                availableExercises = listOf(
                    Exercise(1, "Bench Press", "Chest", "Barbell", false),
                    Exercise(2, "Squat", "Legs", "Barbell", false),
                ),
            ),
            onStartWorkout = {},
            onAddExercise = {},
            onAddQuickSet = {},
            onFinishWorkout = {},
            onDiscardWorkout = {},
        )
    }
}

