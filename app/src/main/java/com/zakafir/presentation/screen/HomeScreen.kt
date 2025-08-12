package com.zakafir.presentation.screen


import androidx.compose.material3.TextButton

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import com.zakafir.presentation.PrayerUiState
import com.zakafir.presentation.StreakHeader
import com.zakafir.presentation.component.HelperCard
import com.zakafir.presentation.component.StatChip
import com.zakafir.presentation.component.TonightCard
import kotlin.run

data class NapConfig(
    val start: String,
    val durationMin: Int
)

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
        // Streak
        item {
            StreakHeader(streak = vmUiState.streak, weeklyGoal = vmUiState.weeklyGoal)
        }
        // Section: Today & Tomorrow
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vmUiState.prayers?.let { prayers ->
                    val list = prayers.prayerTimes
                    val today = list.getOrNull(0)
                    val tomorrow = list.getOrNull(1)

                    today?.let {
                        SectionTitle("Today's prayers")
                        Text(
                            text = today.date ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
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
                        Text(
                            text = tomorrow.date.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
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
                        vmUiState.isLoading -> Text(
                            text = "Loading...",
                            modifier = Modifier.fillMaxSize()
                        )

                        vmUiState.error != null -> Text(
                            text = vmUiState.error,
                            modifier = Modifier.fillMaxSize()
                        )

                        else -> Text(text = "No data", modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        // Section: Qiyam (Tonight)
        item {
            vmUiState.qiyamWindow?.let { qiyam ->
                vmUiState.prayers?.let { prayers ->
                    val list = prayers.prayerTimes
                    val today = list.getOrNull(0)
                    TonightCard(
                        date = today?.date ?: "",
                        window = qiyam,
                        onSchedule = onSchedule,
                        onTestAlarmUi = onTestAlarmUi
                    )
                }
            }
        }

        // Section: Sleep Planner
        item {
            TextButton(onClick = onOpenSettings) {
                Text("Adjust sleep settings")
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("Fajr", fajr, isHighlighted = highlight.contains("Fajr"))
                StatChip("Dhuhr", dhuhr)
                StatChip("Asr", asr)
                StatChip("Maghrib", maghrib, isHighlighted = highlight.contains("Maghrib"))
                StatChip("Isha", isha)
            }
        }
    }
}