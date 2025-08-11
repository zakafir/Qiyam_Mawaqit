package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zakafir.qiyam_mawaqit.presentation.PrayerUiState
import com.zakafir.qiyam_mawaqit.presentation.component.HelperCard
import com.zakafir.qiyam_mawaqit.presentation.component.StatChip
import com.zakafir.qiyam_mawaqit.presentation.component.TonightCard
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
        // Section: Today & Tomorrow
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vmUiState.prayers?.let { prayers ->
                    val list = prayers.prayerTimes
                    val today = list.getOrNull(0)
                    val tomorrow = list.getOrNull(1)

                    today?.let {
                        SectionTitle("Today's prayers")
                        PrayerRowCard(
                            fajr = it.fajr,
                            dhuhr = it.dohr,
                            asr = it.asr,
                            maghrib = it.maghreb,
                            isha = it.icha,
                            highlight = setOf("Maghrib")
                        )
                    }
                    tomorrow?.let {
                        SectionTitle("Tomorrow's prayers")
                        PrayerRowCard(
                            fajr = it.fajr,
                            dhuhr = it.dohr,
                            asr = it.asr,
                            maghrib = it.maghreb,
                            isha = it.icha,
                            highlight = setOf("Fajr")
                        )
                    }
                } ?: run {
                    when {
                        vmUiState.isLoading -> Text(text = "Loading...", modifier = Modifier.fillMaxSize())
                        vmUiState.error != null -> Text(text = vmUiState.error, modifier = Modifier.fillMaxSize())
                        else -> Text(text = "No data", modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        // Section: Qiyam (Tonight)
        item {
            vmUiState.qiyamWindow?.let { qiyam ->
                TonightCard(
                    window = qiyam,
                    onSchedule = onSchedule,
                    onTestAlarmUi = onTestAlarmUi
                )
            }
        }

        // Actions row
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(modifier = Modifier.weight(1f), onClick = onOpenHistory) {
                    Icon(Icons.Outlined.Build, contentDescription = null)
                    Text("  History")
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = null)
                    Text("  Settings")
                }
            }
        }

        // Helper / Education
        item {
            HelperCard(
                title = "What is the last third?",
                body = "It’s the final third of the night between Maghrib and Fajr. Your wake time sits near the center, minus a small buffer for wuḍūʾ."
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun PrayerRowCard(
    fajr: String,
    dhuhr: String,
    asr: String,
    maghrib: String,
    isha: String,
    highlight: Set<String>
) {
    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Fajr", fajr, isHighlighted = highlight.contains("Fajr"))
                StatChip("Dhuhr", dhuhr)
                StatChip("Asr", asr)
                StatChip("Maghrib", maghrib, isHighlighted = highlight.contains("Maghrib"))
                StatChip("Isha", isha)
            }
        }
    }
}

@Composable
private fun QiyamCard(
    start: String,
    end: String,
    onSchedule: () -> Unit,
) {
    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tonight’s Qiyam",)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip("Qiyam start", start)
                StatChip("Qiyam end", end)
            }
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(modifier = Modifier.weight(1f), onClick = onSchedule) {
                    Icon(Icons.Outlined.Phone, contentDescription = null)
                    Text("  Schedule wake-up")
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onSchedule) {
                    Text("Preview alarm")
                }
            }
        }
    }
}