package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakafir.qiyam_mawaqit.presentation.component.HelperCard
import com.zakafir.qiyam_mawaqit.presentation.component.SettingCard

@Composable
fun SettingsScreen(
    bufferMinutes: Int,
    weeklyGoal: Int,
    onBufferChange: (Int) -> Unit,
    onGoalChange: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { SettingCard("Buffer for wuḍūʾ", "$bufferMinutes min") { SliderRow(value = bufferMinutes, range = 0..30, onChange = onBufferChange) } }
        item { SettingCard("Weekly goal", "$weeklyGoal nights") { SliderRow(value = weeklyGoal, range = 1..7, onChange = onGoalChange) } }
        item { HelperCard("Reliability tips", "Enable exact alarms on Android or allow notifications on iOS for best wake‑ups.") }
    }
}

@Composable
private fun SliderRow(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    var local by remember { mutableIntStateOf(value) }
    Column {
        Slider(
            value = local.toFloat(),
            onValueChange = { local = it.toInt() },
            onValueChangeFinished = { onChange(local) },
            valueRange = range.first.toFloat()..range.last.toFloat()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${range.first}")
            Text("$local")
            Text("${range.last}")
        }
    }
}