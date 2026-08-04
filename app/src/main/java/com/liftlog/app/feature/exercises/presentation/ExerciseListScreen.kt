package com.liftlog.app.feature.exercises.presentation

import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.core.model.Exercise
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.ExerciseDraft
import com.liftlog.app.core.ui.theme.LiftLogTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExerciseListRoute(
    onExerciseSelected: (Long) -> Unit,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseListScreen(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onAddCustomExercise = viewModel::addCustomExercise,
        onUpdateExercise = viewModel::updateExercise,
        onDeleteExercise = viewModel::deleteExercise,
        onExerciseSelected = onExerciseSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    state: ExerciseListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAddCustomExercise: (ExerciseDraft) -> Unit,
    onUpdateExercise: (Long, ExerciseDraft) -> Unit,
    onDeleteExercise: (Long) -> Unit,
    onExerciseSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addDialogVisible by remember { mutableStateOf(false) }
    var exercisePendingEdit by remember { mutableStateOf<Exercise?>(null) }
    var exercisePendingDelete by remember { mutableStateOf<Exercise?>(null) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { addDialogVisible = true }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add exercise",
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                label = { Text("Search exercises") },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = state.exercises,
                    key = { exercise -> exercise.id },
                ) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onClick = { onExerciseSelected(exercise.id) },
                        onEdit = { exercisePendingEdit = exercise },
                        onDelete = { exercisePendingDelete = exercise },
                    )
                }
            }
        }
    }

    if (addDialogVisible) {
        CustomExerciseDialog(
            onDismiss = { addDialogVisible = false },
            locations = state.locations,
            onSave = { draft ->
                onAddCustomExercise(draft)
                addDialogVisible = false
            },
        )
    }

    exercisePendingEdit?.let { exercise ->
        CustomExerciseDialog(
            exercise = exercise,
            locations = state.locations,
            onDismiss = { exercisePendingEdit = null },
            onSave = { draft ->
                onUpdateExercise(exercise.id, draft)
                exercisePendingEdit = null
            },
        )
    }

    exercisePendingDelete?.let { exercise ->
        AlertDialog(
            onDismissRequest = { exercisePendingDelete = null },
            title = { Text("Delete ${exercise.name}?") },
            text = { Text("This also removes its workout entries and template references.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExercise(exercise.id)
                    exercisePendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { exercisePendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
internal fun CustomExerciseDialog(
    exercise: Exercise? = null,
    locations: List<String>,
    onDismiss: () -> Unit,
    onSave: (ExerciseDraft) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(exercise?.name.orEmpty()) }
    var muscle by remember { mutableStateOf(exercise?.primaryMuscle.orEmpty()) }
    var equipment by remember { mutableStateOf(exercise?.equipment.orEmpty()) }
    var category by remember { mutableStateOf(exercise?.category ?: ExerciseCategory.FreeWeights) }
    var gymLocation by remember { mutableStateOf(exercise?.gymLocation.orEmpty()) }
    var youTubeUrl by remember { mutableStateOf(exercise?.youTubeUrl.orEmpty()) }
    var imageUri by remember { mutableStateOf(exercise?.imageUri) }
    var imageError by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            imageError = null
            val embeddedImage = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bytes = stream.readBytes()
                        require(bytes.size <= 3 * 1024 * 1024) { "The photo is larger than 3 MB." }
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        "data:$mimeType;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
                    } ?: error("Unable to read the selected photo.")
                }
            }.getOrElse { error ->
                imageError = error.message ?: "Unable to add the selected photo."
                null
            }
            imageUri = embeddedImage
        }
    }
    val isValid = name.isNotBlank() && muscle.isNotBlank() && equipment.isNotBlank() &&
        (category == ExerciseCategory.FreeWeights || gymLocation.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exercise == null) "New exercise" else "Edit exercise") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Exercise name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = muscle,
                    onValueChange = { muscle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Primary muscle") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Equipment") },
                    singleLine = true,
                )
                Text(
                    text = "Exercise type",
                    style = MaterialTheme.typography.labelLarge,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ExerciseCategory.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = category == option,
                            onClick = { category = option },
                            shape = SegmentedButtonDefaults.itemShape(index, ExerciseCategory.entries.size),
                            label = {
                                Text(if (option == ExerciseCategory.FreeWeights) "Free weights" else "Machine")
                            },
                        )
                    }
                }
                if (category == ExerciseCategory.Machine) {
                    Text("Location", style = MaterialTheme.typography.labelLarge)
                    if (locations.isEmpty()) {
                        Text(
                            "Add a location first in the Locations tab.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                            items(locations, key = { it }) { location ->
                                FilterChip(
                                    selected = gymLocation.equals(location, ignoreCase = true),
                                    onClick = { gymLocation = location },
                                    label = { Text(location) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = youTubeUrl,
                    onValueChange = { youTubeUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("YouTube link (optional)") },
                    singleLine = true,
                )
                OutlinedButton(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null)
                    Text(
                        text = if (imageUri == null) "Add photo" else "Photo selected",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (imageUri != null) {
                    TextButton(onClick = { imageUri = null }) { Text("Remove photo") }
                }
                imageError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ExerciseDraft(
                            name = name,
                            primaryMuscle = muscle,
                            equipment = equipment,
                            category = category,
                            gymLocation = gymLocation.takeIf { category == ExerciseCategory.Machine },
                            youTubeUrl = youTubeUrl,
                            imageUri = imageUri,
                        ),
                    )
                },
                enabled = isValid,
            ) { Text(if (exercise == null) "Add" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = listOfNotNull(
                        exercise.primaryMuscle,
                        exercise.equipment,
                        exercise.gymLocation,
                    ).joinToString(" / "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit exercise")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete exercise")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ExerciseListScreenPreview() {
    LiftLogTheme {
        ExerciseListScreen(
            state = ExerciseListUiState(
                exercises = listOf(
                    Exercise(
                        id = 1,
                        name = "Bench Press",
                        primaryMuscle = "Chest",
                        equipment = "Barbell",
                        category = com.liftlog.app.core.model.ExerciseCategory.FreeWeights,
                        gymLocation = null,
                        youTubeUrl = null,
                        imageUri = null,
                        isCustom = false,
                    ),
                    Exercise(
                        id = 2,
                        name = "Dumbbell Row",
                        primaryMuscle = "Back",
                        equipment = "Dumbbell",
                        category = com.liftlog.app.core.model.ExerciseCategory.FreeWeights,
                        gymLocation = null,
                        youTubeUrl = null,
                        imageUri = null,
                        isCustom = false,
                    ),
                ),
            ),
            onSearchQueryChanged = {},
            onAddCustomExercise = {},
            onUpdateExercise = { _, _ -> },
            onDeleteExercise = {},
            onExerciseSelected = {},
        )
    }
}
