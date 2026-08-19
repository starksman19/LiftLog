package com.liftlog.app.feature.progress.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.ExerciseProgress
import com.liftlog.app.core.model.SessionVolume
import com.liftlog.app.core.ui.localization.t
import java.text.DateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale

@Composable
fun ProgressRoute(
    onWorkoutHistory: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressScreen(state = state, onRangeChanged = viewModel::setRange, onWorkoutHistory = onWorkoutHistory)
}

@Composable
fun ProgressScreen(
    state: ProgressUiState,
    onRangeChanged: (Int) -> Unit,
    onWorkoutHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = t("Progress"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(7, 30).forEachIndexed { index, range ->
                    SegmentedButton(
                        selected = state.selectedRange == range,
                        onClick = { onRangeChanged(range) },
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        label = { Text(t("$range workouts", "$range treningów")) },
                    )
                }
            }
        }

        item {
            TrainingTrendCard(sessions = state.recentVolumes)
        }

        item {
            DashboardStats(sessions = state.recentVolumes, modifier = Modifier.fillMaxWidth())
        }

        item {
            OutlinedButton(onClick = onWorkoutHistory, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.History, contentDescription = null)
                Text(t("Full workout history", "Pełna historia treningów"), modifier = Modifier.padding(start = 8.dp))
            }
        }

        item {
            Text(
                text = t("Exercise records", "Rekordy ćwiczeń"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (state.exercises.isEmpty()) {
            item {
                Text(
                    text = t("Finish a workout to see your records here.", "Ukończ trening, aby zobaczyć tutaj swoje rekordy."),
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
private fun TrainingTrendCard(
    sessions: List<SessionVolume>,
    modifier: Modifier = Modifier,
) {
    var grouping by remember { mutableStateOf(TrainingGrouping.Weeks) }
    val periods = remember(sessions, grouping) { sessions.toTrainingPeriods(grouping) }
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
                text = t("Training trend", "Trend treningowy"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (periods.isEmpty()) {
                    t("No completed workouts yet", "Brak ukończonych treningów")
                } else {
                    when (grouping) {
                        TrainingGrouping.Weeks -> t("Completed workouts by week", "Ukończone treningi według tygodni")
                        TrainingGrouping.Months -> t("Completed workouts by month", "Ukończone treningi według miesięcy")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (periods.isNotEmpty()) {
                TrainingTrendChart(
                    periods = periods,
                    grouping = grouping,
                    onToggleGrouping = {
                        grouping = if (grouping == TrainingGrouping.Weeks) {
                            TrainingGrouping.Months
                        } else {
                            TrainingGrouping.Weeks
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TrainingTrendChart(
    periods: List<TrainingPeriod>,
    grouping: TrainingGrouping,
    onToggleGrouping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chartColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val maximum = periods.maxOf { it.workoutCount }.coerceAtLeast(1)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(t("Workouts", "Treningi"), style = MaterialTheme.typography.labelSmall)
            Text(
                text = when (grouping) {
                    TrainingGrouping.Weeks -> t("Weeks", "Tygodnie")
                    TrainingGrouping.Months -> t("Months", "Miesiące")
                },
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.height(136.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                Text(maximum.toString(), style = MaterialTheme.typography.labelSmall)
                Text((maximum / 2).toString(), style = MaterialTheme.typography.labelSmall)
                Text("0", style = MaterialTheme.typography.labelSmall)
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(136.dp)
                    .padding(start = 8.dp)
                    .clickable(
                        onClickLabel = t("Change chart period", "Zmień okres wykresu"),
                        onClick = onToggleGrouping,
                    ),
            ) {
                val top = 8.dp.toPx()
                val bottom = size.height - 8.dp.toPx()
                repeat(3) { index ->
                    val y = top + (bottom - top) * index / 2f
                    drawLine(
                        color = guideColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                val xStep = if (periods.size == 1) 0f else size.width / (periods.size - 1)
                val points = periods.mapIndexed { index, period ->
                    Offset(
                        x = if (periods.size == 1) size.width / 2f else index * xStep,
                        y = bottom - (bottom - top) * period.workoutCount / maximum,
                    )
                }
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { point -> lineTo(point.x, point.y) }
                    }
                    drawPath(path = path, color = chartColor, style = Stroke(width = 2.dp.toPx()))
                }
                points.forEach { point ->
                    drawCircle(
                        color = chartColor,
                        radius = 4.dp.toPx(),
                        center = point,
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(periods.first().startDate.trendDateText(grouping), style = MaterialTheme.typography.labelSmall)
            Text(periods.last().startDate.trendDateText(grouping), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DashboardStats(
    sessions: List<SessionVolume>,
    modifier: Modifier = Modifier,
) {
    val activeDays = remember(sessions) { sessions.toActivityDays().size }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DashboardStat(
            label = t("Completed workouts", "Ukończone treningi"),
            value = sessions.size.toString(),
            modifier = Modifier.weight(1f),
        )
        DashboardStat(
            label = t("Active days", "Aktywne dni"),
            value = activeDays.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DashboardStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

private data class ActivityDay(val date: LocalDate, val workoutCount: Int)

private enum class TrainingGrouping {
    Weeks,
    Months,
}

private data class TrainingPeriod(
    val startDate: LocalDate,
    val workoutCount: Int,
)

private fun List<SessionVolume>.toActivityDays(): List<ActivityDay> =
    groupBy { session ->
        Instant.ofEpochMilli(session.startedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
        .toSortedMap()
        .map { (date, sessions) -> ActivityDay(date, sessions.size) }

private fun List<SessionVolume>.toTrainingPeriods(grouping: TrainingGrouping): List<TrainingPeriod> =
    groupBy { session ->
        val date = Instant.ofEpochMilli(session.startedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        when (grouping) {
            TrainingGrouping.Weeks -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            TrainingGrouping.Months -> date.withDayOfMonth(1)
        }
    }
        .toSortedMap()
        .map { (date, sessions) -> TrainingPeriod(date, sessions.size) }

private fun LocalDate.activityDateText(): String =
    format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

private fun LocalDate.trendDateText(grouping: TrainingGrouping): String = when (grouping) {
    TrainingGrouping.Weeks -> activityDateText()
    TrainingGrouping.Months -> format(DateTimeFormatter.ofPattern("MMM yy", Locale.getDefault()))
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
                ProgressMetric(t("Best weight", "Najlepszy ciężar"), "${exercise.maxWeight.compact()} kg")
                Spacer(modifier = Modifier.width(20.dp))
                ProgressMetric(t("Best reps", "Najwięcej powtórzeń"), exercise.maxReps.toString())
                Spacer(modifier = Modifier.width(20.dp))
                ProgressMetric(t("Workouts", "Treningi"), exercise.workoutCount.toString())
            }
            Text(
                text = t("Last trained ${exercise.lastPerformedAtEpochMillis.shortDate()}", "Ostatni trening: ${exercise.lastPerformedAtEpochMillis.shortDate()}"),
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
