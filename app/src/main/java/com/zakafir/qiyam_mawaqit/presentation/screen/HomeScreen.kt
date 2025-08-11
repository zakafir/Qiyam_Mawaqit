package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zakafir.qiyam_mawaqit.presentation.PrayerUiState
import com.zakafir.qiyam_mawaqit.presentation.component.HelperCard
import com.zakafir.qiyam_mawaqit.presentation.component.StatChip
import com.zakafir.qiyam_mawaqit.presentation.timeOnly
import kotlinx.datetime.LocalDateTime

@Composable
fun HomeScreen(
    vmUiState: PrayerUiState,
    onSchedule: (LocalDateTime) -> Unit,
    onTestAlarmUi: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            when {
                vmUiState.isLoading -> {
                    Text(text = "Loading...", modifier = Modifier.fillMaxSize())
                }

                vmUiState.error != null -> {
                    Text(text = vmUiState.error, modifier = Modifier.fillMaxSize())
                }

                vmUiState.prayers != null -> {
                    vmUiState.prayers.prayerTimes.forEach { prayerTimes ->
                        Row {
                            StatChip("Fajr", prayerTimes.fajr)
                            StatChip("Dhuhr", prayerTimes.dohr)
                            StatChip("Asr", prayerTimes.asr)
                            StatChip("Maghrib", prayerTimes.maghreb)
                            StatChip("Isha", prayerTimes.icha)
                        }
                    }
                }

                else -> {
                    Text(text = "No data", modifier = Modifier.fillMaxSize())
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                vmUiState.qiyamUiState?.let { qiyamUiState ->
                    StatChip("Qiyam start", timeOnly(qiyamUiState.start))
                    StatChip("Qiyam end", timeOnly(qiyamUiState.end))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                vmUiState.qiyamUiState?.let { qiyamUiState ->
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSchedule(qiyamUiState.suggestedWake) }) {
                        Text("Schedule wake-up")
                    }
                }

                OutlinedButton(modifier = Modifier.weight(1f), onClick = onTestAlarmUi) {
                    Text("Test alarm UI")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(modifier = Modifier.weight(1f), onClick = onOpenHistory) {
                    Text("View history")
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onOpenSettings) {
                    Text("Settings")
                }
            }
        }
        item {
            HelperCard(
                title = "What is the last third?", body =
                    "It's the final third of the night between Maghrib and Fajr. Your wake time sits near the center, minus a small buffer for wuḍūʾ."
            )
        }
    }
}