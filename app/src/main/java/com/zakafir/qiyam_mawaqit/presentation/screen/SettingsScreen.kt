package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    bufferMinutes: Int,
    weeklyGoal: Int,
    onBufferChange: (Int) -> Unit,
    onGoalChange: (Int) -> Unit,
) {
    // Local editable copies (so the slider moves immediately)
    val (buffer, setBuffer) = remember(bufferMinutes) { mutableStateOf(bufferMinutes) }
    val (goal, setGoal) = remember(weeklyGoal) { mutableStateOf(weeklyGoal) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

        // Buffer minutes
        Text(text = "Buffer before Qiyam (minute(s)) — $buffer")
        Slider(
            value = buffer.toFloat(),
            onValueChange = { setBuffer(it.toInt()) },
            valueRange = 0f..60f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { setBuffer(0) }) { Text("Reset") }
            TextButton(onClick = { onBufferChange(buffer) }) { Text("Save") }
        }

        // Weekly goal
        Text(text = "Weekly goal (nights) — $goal")
        Slider(
            value = goal.toFloat(),
            onValueChange = { setGoal(it.toInt()) },
            valueRange = 0f..7f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { setGoal(3) }) { Text("Default") }
            TextButton(onClick = { onGoalChange(goal) }) { Text("Save") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettings() {
    SettingsScreen(
        bufferMinutes = 12,
        weeklyGoal = 3,
        onBufferChange = {},
        onGoalChange = {},
    )
}