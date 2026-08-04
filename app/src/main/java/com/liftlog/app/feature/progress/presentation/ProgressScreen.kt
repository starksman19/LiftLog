package com.liftlog.app.feature.progress.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressRoute(
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressScreen(state = state)
}

@Composable
fun ProgressScreen(
    state: ProgressUiState,
    modifier: Modifier = Modifier,
) {
    val totalVolume = state.recentVolumes.sumOf { it.volume }
    val workouts = state.recentVolumes.size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            VolumeChartCard(volumes = state.recentVolumes)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStat(
                    label = "Last 7 workouts",
                    value = workouts.toString(),
                    modifier = Modifier.weight(1f),
                )
                DashboardStat(
                    label = "Training volume",
                    value = "${totalVolume.compact()} kg",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Text(
                text = "Exercise records",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (state.exercises.isEmpty()) {
            item {
                Text(
                    text = "Finish a workout to see your records here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = state.exercises,
                key = { exercise -> exercise.exerciseId },
            ) { exercise ->
                ExerciseProgressCard(exercise = exercise)
            }
        }
    }
}

@Composable
private fun VolumeChartCard(
    volumes: List<SessionVolume>,
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Workout volume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (volumes.isEmpty()) "No completed workouts yet" else "Last ${volumes.size} workouts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VolumeChart(volumes = volumes)
        }
    }
}

@Composable
private fun VolumeChart(
    volumes: List<SessionVolume>,
    modifier: Modifier = Modifier,
) {
    val chartColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .padding(vertical = 12.dp),
    ) {
        val left = 4.dp.toPx()
        val right = size.width - 4.dp.toPx()
        val top = 4.dp.toPx()
        val bottom = size.height - 4.dp.toPx()

        repeat(3) { index ->
            val y = top + (bottom - top) * index / 2f
            drawLine(
                color = guideColor,
                start = androidx.compose.ui.geometry.Offset(left, y),
                end = androidx.compose.ui.geometry.Offset(right, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        if (volumes.isEmpty()) return@Canvas

        val maxVolume = volumes.maxOf { it.volume }.coerceAtLeast(1.0)
        val horizontalStep = if (volumes.size == 1) 0f else (right - left) / (volumes.size - 1)
        val points = volumes.mapIndexed { index, item ->
            val normalized = (item.volume / maxVolume).toFloat()
            androidx.compose.ui.geometry.Offset(
                x = left + horizontalStep * index,
                y = bottom - (bottom - top) * normalized,
            )
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        }

        drawPath(
            path = path,
            color = chartColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        points.forEach { point ->
            drawCircle(color = chartColor, radius = 4.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun DashboardStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ExerciseProgressCard(
    exercise: ExerciseProgress,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressMetric("Best weight", "${exercise.maxWeight.compact()} kg")
                Spacer(modifier = Modifier.width(20.dp))
                ProgressMetric("Best reps", exercise.maxReps.toString())
                Spacer(modifier = Modifier.width(20.dp))
                ProgressMetric("Workouts", exercise.workoutCount.toString())
            }
            Text(
                text = "Last trained ${exercise.lastPerformedAtEpochMillis.shortDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProgressMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Double.compact(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)
}

private fun Long.shortDate(): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(this))
