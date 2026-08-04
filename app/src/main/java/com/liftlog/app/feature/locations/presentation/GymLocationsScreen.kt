package com.liftlog.app.feature.locations.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.liftlog.app.core.ui.localization.t

@Composable
fun GymLocationsRoute(viewModel: GymLocationsViewModel = hiltViewModel()) {
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    GymLocationsScreen(
        locations = locations,
        onAdd = viewModel::add,
        onRename = viewModel::rename,
        onDelete = viewModel::delete,
    )
}

@Composable
private fun GymLocationsScreen(
    locations: List<String>,
    onAdd: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { adding = true }) {
                Icon(Icons.Outlined.Add, contentDescription = t("Add location"))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = padding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(t("Locations"), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            item {
                Text(
                    t("Choose these locations when creating machine exercises and starting workouts.", "Wybieraj te lokalizacje podczas tworzenia ćwiczeń na maszynach i rozpoczynania treningów."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (locations.isEmpty()) {
                item {
                    Text(
                        t("No locations yet.", "Nie ma jeszcze żadnych lokalizacji."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }
            items(locations, key = { it }) { location ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null)
                            Text(location, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleMedium)
                        }
                        Row {
                            IconButton(onClick = { editing = location }) {
                                Icon(Icons.Outlined.Edit, contentDescription = t("Rename location"))
                            }
                            IconButton(onClick = { deleting = location }) {
                                Icon(Icons.Outlined.Delete, contentDescription = t("Delete location", "Usuń lokalizację"))
                            }
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        LocationNameDialog(
            title = t("New location"),
            onDismiss = { adding = false },
            onSave = { onAdd(it); adding = false },
        )
    }
    editing?.let { location ->
        LocationNameDialog(
            title = t("Rename location"),
            initialName = location,
            onDismiss = { editing = null },
            onSave = { onRename(location, it); editing = null },
        )
    }
    deleting?.let { location ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(t("Delete $location?", "Usunąć $location?")) },
            text = { Text(t("This clears the location from related machines and workouts, but does not delete the workouts.", "Spowoduje to usunięcie lokalizacji z maszyn i treningów, ale nie usunie treningów.")) },
            confirmButton = { TextButton(onClick = { onDelete(location); deleting = null }) { Text(t("Delete")) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(t("Cancel")) } },
        )
    }
}

@Composable
internal fun LocationNameDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t("Location name")) },
                singleLine = true,
            )
        },
        confirmButton = { TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text(t("Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } },
    )
}
