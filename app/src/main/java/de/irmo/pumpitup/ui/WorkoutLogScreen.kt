package de.irmo.pumpitup.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.irmo.pumpitup.Workout
import de.irmo.pumpitup.WorkoutRepository
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogScreen(
    repository: WorkoutRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var workouts by remember { mutableStateOf(repository.getWorkouts().sortedByDescending { it.timestamp }) }
    val context = LocalContext.current

    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editCountText by remember { mutableStateOf("") }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    fun refresh() {
        workouts = repository.getWorkouts().sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { exportWorkouts(context, workouts, formatter) }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workouts, key = { it.id }) { workout ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${workout.count} Pushups",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatter.format(Date(workout.timestamp)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                selectedWorkout = workout
                                editCountText = workout.count.toString()
                                showEditDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                repository.deleteWorkout(workout.id)
                                refresh()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedWorkout != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Pushups") },
            text = {
                OutlinedTextField(
                    value = editCountText,
                    onValueChange = { editCountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newCount = editCountText.toIntOrNull()
                    if (newCount != null) {
                        repository.updateWorkout(selectedWorkout!!.id, newCount)
                        refresh()
                    }
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun exportWorkouts(context: Context, workouts: List<Workout>, formatter: SimpleDateFormat) {
    try {
        val file = File(context.cacheDir, "workouts_export.csv")
        FileWriter(file).use { writer ->
            writer.append("Date,Time,Pushups\n")
            for (workout in workouts) {
                val dateStr = formatter.format(Date(workout.timestamp))
                val parts = dateStr.split(" ")
                val date = parts.getOrElse(0) { "" }
                val time = parts.getOrElse(1) { "" }
                writer.append("$date,$time,${workout.count}\n")
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Pushup Workouts")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Workouts"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
