package com.liftlog.app.feature.exercises.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.liftlog.app.core.ui.localization.t
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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
        onSortModeChanged = viewModel::onSortModeChanged,
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
    onSortModeChanged: (ExerciseSortMode) -> Unit,
    onExerciseSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addDialogVisible by remember { mutableStateOf(false) }
    var exercisePendingEdit by remember { mutableStateOf<Exercise?>(null) }
    var exercisePendingDelete by remember { mutableStateOf<Exercise?>(null) }
    var sortMenuVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.sortMode) {
        listState.scrollToItem(0)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { addDialogVisible = true }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = t("Add exercise", "Dodaj ćwiczenie"),
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = t("Exercises"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Box {
                    IconButton(onClick = { sortMenuVisible = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = t("Sort", "Sortuj"))
                    }
                    DropdownMenu(expanded = sortMenuVisible, onDismissRequest = { sortMenuVisible = false }) {
                        ExerciseSortMode.entries.forEach { sortMode ->
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
                label = { Text(t("Search exercises")) },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
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
            onSave = { draft ->
                onAddCustomExercise(draft)
                addDialogVisible = false
            },
        )
    }

    exercisePendingEdit?.let { exercise ->
        CustomExerciseDialog(
            exercise = exercise,
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
            title = { Text(t("Delete ${exercise.name}?", "Usunąć ${exercise.name}?")) },
            text = { Text(t("This also removes its workout entries and template references.", "Usunie to także wpisy tego ćwiczenia z treningów i szablonów.")) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExercise(exercise.id)
                    exercisePendingDelete = null
                }) { Text(t("Delete")) }
            },
            dismissButton = { TextButton(onClick = { exercisePendingDelete = null }) { Text(t("Cancel")) } },
        )
    }
}

@Composable
internal fun CustomExerciseDialog(
    exercise: Exercise? = null,
    onDismiss: () -> Unit,
    onSave: (ExerciseDraft) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(exercise?.name.orEmpty()) }
    var muscle by remember { mutableStateOf(exercise?.primaryMuscle.orEmpty()) }
    var equipment by remember { mutableStateOf(exercise?.equipment.orEmpty()) }
    var category by remember { mutableStateOf(exercise?.category ?: ExerciseCategory.FreeWeights) }
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
                        require(bytes.size <= 20 * 1024 * 1024) { "The photo is larger than 20 MB." }
                        bytes.toCompressedImageDataUrl()
                    } ?: error("Unable to read the selected photo.")
                }
            }.getOrElse { error ->
                imageError = error.message ?: "Unable to add the selected photo."
                null
            }
            imageUri = embeddedImage
        }
    }
    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t(if (exercise == null) "New exercise" else "Edit exercise")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Exercise name")) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = muscle,
                    onValueChange = { muscle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Primary muscle (optional)", "Główna partia mięśniowa (opcjonalnie)")) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("Equipment (optional)", "Sprzęt / maszyna (opcjonalnie)")) },
                    singleLine = true,
                )
                Text(
                    text = t("Exercise type"),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExerciseCategory.entries.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            modifier = Modifier.weight(1f).height(56.dp),
                            label = {
                                Text(
                                    text = option.choiceLabel(),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                }
                OutlinedTextField(
                    value = youTubeUrl,
                    onValueChange = { youTubeUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t("YouTube link (optional)")) },
                    singleLine = true,
                )
                OutlinedButton(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null)
                    Text(
                        text = t(if (imageUri == null) "Add photo" else "Photo selected"),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (imageUri != null) {
                    TextButton(onClick = { imageUri = null }) { Text(t("Remove photo")) }
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
                            gymLocation = null,
                            youTubeUrl = youTubeUrl,
                            imageUri = imageUri,
                        ),
                    )
                },
                enabled = isValid,
            ) { Text(t(if (exercise == null) "Add" else "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t("Cancel")) }
        },
    )
}

@Composable
private fun ExerciseCategory.localizedLabel(): String = when (this) {
    ExerciseCategory.FreeWeights -> t("Free weights")
    ExerciseCategory.Machine -> t("Machine")
    ExerciseCategory.Timed -> t("Timed")
}

@Composable
private fun ExerciseCategory.choiceLabel(): String = when (this) {
    ExerciseCategory.FreeWeights -> t("Free\nweights", "Wolne\nciężary")
    ExerciseCategory.Machine -> t("Machine", "Maszyna")
    ExerciseCategory.Timed -> t("Timed", "Na czas")
}

@Composable
private fun ExerciseSortMode.label(): String = when (this) {
    ExerciseSortMode.NameAscending -> t("Name A-Z", "Nazwa A-Z")
    ExerciseSortMode.NameDescending -> t("Name Z-A", "Nazwa Z-A")
    ExerciseSortMode.Category -> t("Category", "Kategoria")
}

private fun ByteArray.toCompressedImageDataUrl(): String {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "The selected file is not a supported image." }

    val bitmap = BitmapFactory.decodeByteArray(
        this,
        0,
        size,
        BitmapFactory.Options().apply {
            inSampleSize = imageSampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        },
    ) ?: error("Unable to decode the selected photo.")
    val orientedBitmap = bitmap.withExifOrientation(this)
    if (orientedBitmap !== bitmap) bitmap.recycle()
    val scaledBitmap = orientedBitmap.scaleDownTo(1_920)
    val output = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
    if (scaledBitmap !== orientedBitmap) orientedBitmap.recycle()
    scaledBitmap.recycle()
    return "data:image/jpeg;base64,${Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)}"
}

private fun Bitmap.withExifOrientation(source: ByteArray): Bitmap {
    val orientation = ByteArrayInputStream(source).use { stream ->
        ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                postRotate(90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                postRotate(270f)
                postScale(-1f, 1f)
            }
        }
    }
    return if (matrix.isIdentity) this else Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun imageSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (maxOf(width / sampleSize, height / sampleSize) > 1_920) sampleSize *= 2
    return sampleSize
}

private fun Bitmap.scaleDownTo(maxDimension: Int): Bitmap {
    val largestDimension = maxOf(width, height)
    if (largestDimension <= maxDimension) return this
    val scale = maxDimension.toFloat() / largestDimension
    return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val details = listOf(
                    exercise.category.localizedLabel(),
                    exercise.primaryMuscle,
                    exercise.equipment,
                ).filter { it.isNotBlank() }.joinToString(" / ")
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = t("Edit exercise"))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = t("Delete exercise", "Usuń ćwiczenie"))
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
            onSortModeChanged = {},
            onExerciseSelected = {},
        )
    }
}
