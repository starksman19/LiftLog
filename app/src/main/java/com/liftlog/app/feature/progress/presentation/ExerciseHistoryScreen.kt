package com.liftlog.app.feature.progress.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseHistoryRoute(
    exerciseId: Long,
    onBack: () -> Unit,
    viewModel: ExerciseHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState(exerciseId).collectAsStateWithLifecycle()
    ExerciseHistoryScreen(state = state, onBack = onBack)
}

@Composable
fun ExerciseHistoryScreen(
    state: ExerciseHistoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = state.exerciseName.ifBlank { "Exercise history" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        state.exercise?.let { exercise ->
            item {
                ExerciseInformationCard(
                    exercise = exercise,
                    onOpenVideo = { link -> runCatching { uriHandler.openUri(link) } },
                )
            }
        }

        if (state.history.isNotEmpty()) {
            item { ExerciseWeightChart(state.history) }
        }

        if (state.history.isEmpty()) {
            item {
                Text(
                    text = "No completed sets for this exercise yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = state.history,
                key = { session -> session.finishedAtEpochMillis },
            ) { session ->
                HistorySessionCard(session = session)
            }
        }
    }
}

@Composable
private fun ExerciseWeightChart(history: List<ExerciseHistorySession>) {
    val points = history.asReversed().map { session ->
        session.finishedAtEpochMillis to session.sets.maxOf { it.weight }
    }
    val chartColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Best weight progression", fontWeight = FontWeight.SemiBold)
            Canvas(modifier = Modifier.fillMaxWidth().height(132.dp)) {
                if (points.isEmpty()) return@Canvas
                val left = 4.dp.toPx()
                val right = size.width - 4.dp.toPx()
                val top = 4.dp.toPx()
                val bottom = size.height - 4.dp.toPx()
                val max = points.maxOf { it.second }.coerceAtLeast(1.0)
                val step = if (points.size == 1) 0f else (right - left) / (points.size - 1)
                val offsets = points.mapIndexed { index, (_, weight) ->
                    androidx.compose.ui.geometry.Offset(left + step * index, bottom - ((weight / max).toFloat() * (bottom - top)))
                }
                val path = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, color = chartColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                offsets.forEach { drawCircle(chartColor, 4.dp.toPx(), it) }
            }
            Text("Latest best: ${points.last().second.compact()} kg", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ExerciseInformationCard(
    exercise: com.liftlog.app.core.model.Exercise,
    onOpenVideo: (String) -> Unit,
) {
    val image = androidx.compose.runtime.remember(exercise.imageUri) {
        exercise.imageUri
            ?.substringAfter("base64,", missingDelimiterValue = "")
            ?.takeIf { it.isNotEmpty() }
            ?.let { encoded ->
                runCatching {
                    val bytes = Base64.decode(encoded, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }
                    .getOrNull()
            }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${exercise.primaryMuscle} / ${exercise.equipment}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (exercise.category == com.liftlog.app.core.model.ExerciseCategory.Machine) "Machine" else "Free weights")
            exercise.gymLocation?.let { Text("Location: $it") }
            image?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Exercise photo",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            exercise.youTubeUrl?.let { link ->
                IconButton(onClick = { onOpenVideo(link) }) {
                    Icon(Icons.Outlined.PlayCircle, contentDescription = "Open YouTube instruction")
                }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: ExerciseHistorySession,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(session.finishedAtEpochMillis)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            session.sets.forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Set ${set.setNumber}")
                    Text(
                        text = "${set.weight.compact()} kg x ${set.reps}",
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private fun Double.compact(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)
}
