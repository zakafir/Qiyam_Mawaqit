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
import com.zakafir.qiyam_mawaqit.presentation.QiyamUiState
import com.zakafir.qiyam_mawaqit.presentation.StreakHeader
import com.zakafir.qiyam_mawaqit.presentation.component.HelperCard
import com.zakafir.qiyam_mawaqit.presentation.component.StatChip
import com.zakafir.qiyam_mawaqit.presentation.component.TonightCard
import kotlinx.datetime.LocalDateTime

@Composable
fun HomeScreen(
    vmUiState: PrayerUiState,
    qiyamUiState: QiyamUiState,
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
            vmUiState.qiyamWindow?.let { qiyam ->
                Row {
                    StatChip("Last third start", qiyam.start)
                    StatChip("Last third end", qiyam.end)
                }
            } ?: run {
                Text(
                    text = if (vmUiState.isLoading) "Loading..." else "",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        item {
            StreakHeader(streak = qiyamUiState.streakDays, weeklyGoal = qiyamUiState.weeklyGoal)
        }
        item {
            TonightCard(
                window = qiyamUiState.window,
                onSchedule = onSchedule,
                onTestAlarmUi = onTestAlarmUi
            )
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