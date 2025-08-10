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
import com.zakafir.qiyam_mawaqit.presentation.QiyamUiState
import com.zakafir.qiyam_mawaqit.presentation.StreakHeader
import com.zakafir.qiyam_mawaqit.presentation.TonightCard
import com.zakafir.qiyam_mawaqit.presentation.component.HelperCard
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen(
    state: QiyamUiState,
    onSchedule: (LocalDateTime) -> Unit,
    onTestAlarmUi: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val zonedNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val todayLabel = state.today.toString()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StreakHeader(streak = state.streakDays, weeklyGoal = state.weeklyGoal)
        }
        item {
            TonightCard(
                window = state.window,
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
            HelperCard(title = "What is the last third?", body =
                "It's the final third of the night between Maghrib and Fajr. Your wake time sits near the center, minus a small buffer for wuḍūʾ.")
        }
    }
}