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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
                    val prayersList = vmUiState.prayers.prayerTimes
                    val today = prayersList.getOrNull(0)
                    val tomorrow = prayersList.getOrNull(1)
                    if (today != null) {
                        Text(
                            text = "Today's prayers",
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row {
                            StatChip("Fajr", today.fajr)
                            StatChip("Dhuhr", today.dohr)
                            StatChip("Asr", today.asr)
                            // Highlight Maghrib (today)
                            StatChip("Maghrib", value = today.maghreb, isHighlighted = true)
                            StatChip("Isha", today.icha)
                        }
                    }
                    if (tomorrow != null) {
                        Text(
                            text = "Tomorrow's prayers",
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        Row {
                            // Highlight Fajr (tomorrow)
                            StatChip("Fajr", value = tomorrow.fajr, isHighlighted = true)
                            StatChip("Dhuhr", tomorrow.dohr)
                            StatChip("Asr", tomorrow.asr)
                            StatChip("Maghrib", tomorrow.maghreb)
                            StatChip("Isha", tomorrow.icha)
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