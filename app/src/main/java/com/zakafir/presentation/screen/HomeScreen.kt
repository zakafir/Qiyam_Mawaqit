package com.zakafir.presentation.screen


import androidx.compose.foundation.clickable
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.zakafir.domain.model.PrayerTimes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DatePeriod
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
fun MasjidPicker(
    ui: PrayerUiState,
    modifier: Modifier = Modifier,
    updateMasjidId: (String) -> Unit,
    selectMasjidSuggestion: (String?) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    var isFocused by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            value = ui.masjidId,
            onValueChange = {
                isFocused = true
                updateMasjidId.invoke(it)
            },
            label = { Text("Masjid (Mawaqit)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        )

        val showSuggestions = isFocused && ui.searchResults.isNotEmpty()
        if (showSuggestions) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp) // height of the text field
            ) {
                Column {
                    ui.searchResults.forEach { item ->
                        val title = item
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isFocused = false
                                    keyboardController?.hide()
                                    item.second?.let {
                                        selectMasjidSuggestion(it)
                                        updateMasjidId(it)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                title.first?.let { title ->
                                    Text(title, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    vmUiState: PrayerUiState,
    onMasjidIdChange: (String) -> Unit,
    onSchedule: (LocalDateTime) -> Unit,
    onTestAlarmUi: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectMasjidSuggestion: (String?) -> Unit,
    onComputeQiyam: (PrayerTimes?, PrayerTimes?) -> Unit,
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
        // Section: Mawaqit Masjid ID
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MasjidPicker(
                    ui = vmUiState,
                    updateMasjidId = onMasjidIdChange,
                    selectMasjidSuggestion = onSelectMasjidSuggestion
                )
                Text(
                    text = vmUiState.dataSourceLabel ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        // Section: Today & Tomorrow
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vmUiState.yearlyPrayers?.let { prayers ->
                    val keyboardController = LocalSoftwareKeyboardController.current
                    keyboardController?.hide()

                    val list = prayers.prayerTimes

                    // Figure out "today" based on the device timezone and pick entries from the yearly calendar
                    val todayDate =
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val todayStr = todayDate.toString() // ISO-8601 yyyy-MM-dd

                    // Find the first entry that matches today's date
                    val todayIndex = list.indexOfFirst { it.date == todayStr }

                    val today = list.getOrNull(todayIndex)
                    val tomorrow = list.getOrNull(todayIndex + 1)
                    onComputeQiyam(today, tomorrow)

                    today?.let {
                        SectionTitle("Today's prayers")
                        Text(
                            text = it.date ?: "",
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
                            text = it.date?.toString() ?: "",
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
                vmUiState.yearlyPrayers?.let { prayers ->
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