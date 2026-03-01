package de.irmo.pumpitup.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.irmo.pumpitup.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var upperThreshold by remember { mutableFloatStateOf(settingsRepository.getUpperThreshold()) }
    var lowerThreshold by remember { mutableFloatStateOf(settingsRepository.getLowerThreshold()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Pushup Detection Limits",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Upper Threshold (Bottom of pushup)
            Text(
                text = "Bottom of pushup (Distance)",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Gets triggered when face area > ${(upperThreshold * 100).toInt()}% of frame.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = upperThreshold,
                onValueChange = { 
                    upperThreshold = it
                    settingsRepository.setUpperThreshold(it)
                },
                valueRange = 0.10f..0.75f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Lower Threshold (Top of pushup)
            Text(
                text = "Top of pushup (Distance)",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Completes rep when face area < ${(lowerThreshold * 100).toInt()}% of frame.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = lowerThreshold,
                onValueChange = { 
                    lowerThreshold = it
                    settingsRepository.setLowerThreshold(it)
                },
                valueRange = 0.05f..0.50f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Debounce Time
            var debounceTime by remember { mutableFloatStateOf(settingsRepository.getDebounceTime().toFloat()) }
            Text(
                text = "Debounce Time",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Minimum time between reps: ${debounceTime.toInt()}ms",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = debounceTime,
                onValueChange = { 
                    debounceTime = it
                    settingsRepository.setDebounceTime(it.toLong())
                },
                valueRange = 200f..1500f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
