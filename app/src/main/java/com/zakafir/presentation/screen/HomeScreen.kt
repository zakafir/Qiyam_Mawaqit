package com.zakafir.presentation.screen


import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamMode
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
    onOpenDetailsScreen: (MosqueDetails) -> Unit,
    enabled: Boolean = true
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    var isFocused by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            value = ui.selectedMosque?.displayLine ?: ui.masjidId,
            onValueChange = {
                isFocused = true
                updateMasjidId.invoke(it)
            },
            label = { Text("Masjid (Mawaqit)") },
            singleLine = true,
            enabled = enabled,
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
                    ui.searchResults.forEach { mosqueDetails ->
                        val title = mosqueDetails.displayLine
                        val slug = mosqueDetails.slug
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isFocused = false
                                    keyboardController?.hide()
                                    selectMasjidSuggestion(slug)
                                    updateMasjidId(slug)
                                    onOpenDetailsScreen(mosqueDetails)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(title, style = MaterialTheme.typography.bodyLarge)
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
    onSelectMasjidSuggestion: (String?) -> Unit,
    onComputeQiyam: (PrayerTimes?, PrayerTimes?) -> Unit,
    onModeChange: (QiyamMode) -> Unit,
    onOpenDetailsScreen: (MosqueDetails) -> Unit
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
            var editEnabled by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MasjidPicker(
                    ui = vmUiState,
                    updateMasjidId = onMasjidIdChange,
                    selectMasjidSuggestion = onSelectMasjidSuggestion,
                    onOpenDetailsScreen = onOpenDetailsScreen,
                    enabled = editEnabled,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = {
                        editEnabled = true
                        focusRequester.requestFocus()
                    }) {
                        Text("Update")
                    }
                    OutlinedButton(onClick = {
                        onMasjidIdChange("")
                        onSelectMasjidSuggestion(null)
                        editEnabled = false
                    }) {
                        Text("Remove")
                    }
                }
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
            vmUiState.qiyamUiState?.let { qiyamUiState ->
                vmUiState.yearlyPrayers?.let { prayers ->
                    val list = prayers.prayerTimes
                    val today = list.getOrNull(0)
                    TonightCard(
                        date = today?.date ?: "",
                        qiyamUiState = qiyamUiState,
                        onSchedule = onSchedule,
                        onTestAlarmUi = onTestAlarmUi,
                        onModeChange = onModeChange
                    )
                }
            }
        }

        // Helper / Education
        vmUiState.qiyamUiState?.mode?.let { mode ->
            item {
                val helperBody = when (mode) {
                    is QiyamMode.AfterIsha -> "From after Isha until Fajr — any time in this window counts as Qiyam."
                    is QiyamMode.LastHalf -> "The last half of the night between Maghrib and Fajr. Preferred if you can wake up in this period."
                    is QiyamMode.LastThird -> "The final third of the night between Maghrib and Fajr. Your wake time sits near the center, minus a small buffer for wuḍūʾ."
                    is QiyamMode.Dawud -> "Following the prayer of Dawud: sleep half the night, pray one third, then sleep one sixth. This covers the 4th and 5th sixths of the night."
                }
                HelperCard(
                    title = "What is the $mode mode?",
                    body = helperBody
                )
            }
        }
    }
}


@Composable
fun QiyamModeSelector(
    mode: QiyamMode,
    onSelect: (QiyamMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeChip(
            text = "last third",
            selected = mode is QiyamMode.LastThird,
            onClick = { onSelect(QiyamMode.LastThird) }
        )
        ModeChip(
            text = "Dawoud",
            selected = mode is QiyamMode.Dawud,
            onClick = { onSelect(QiyamMode.Dawud) }
        )
        ModeChip(
            text = "after Isha",
            selected = mode is QiyamMode.AfterIsha,
            onClick = { onSelect(QiyamMode.AfterIsha) }
        )
        ModeChip(
            text = "last half",
            selected = mode is QiyamMode.LastHalf,
            onClick = { onSelect(QiyamMode.LastHalf) }
        )
    }
}

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        FilledTonalButton(onClick = onClick) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick) { Text(text) }
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