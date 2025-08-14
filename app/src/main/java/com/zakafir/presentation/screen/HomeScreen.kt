package com.zakafir.presentation.screen


import androidx.compose.foundation.clickable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DockedSearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamMode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.zakafir.presentation.PrayerUiState
import com.zakafir.presentation.component.StatChip
import com.zakafir.presentation.component.TonightCard
import kotlin.run
import androidx.compose.ui.text.style.TextAlign
import com.zakafir.presentation.QiyamAlarm

data class NapConfig(
    val start: String,
    val durationMin: Int
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizableSearchBar(
    modifier: Modifier = Modifier,
    ui: PrayerUiState,
    updateMasjidId: (String) -> Unit,
    selectMasjidSuggestion: (String?) -> Unit,
    onOpenDetailsScreen: (MosqueDetails) -> Unit,
    // Customization options
    placeholder: @Composable () -> Unit = { Text("Search Masjid using Mawaqit") },
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingContent: (@Composable (String) -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    // Track expanded (active) state
    var active by rememberSaveable { mutableStateOf(false) }
    var requestedActive by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var previousText by rememberSaveable { mutableStateOf( ui.selectedMosque?.displayLine ?: ui.masjidId ) }
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(ui.selectedMosque?.displayLine ?: ui.masjidId))
    }

    LaunchedEffect(ui.searchResults) {
        // Expand only if the user requested it and there are results
        active = requestedActive && ui.searchResults.isNotEmpty()
    }

    DockedSearchBar(
        modifier = modifier.fillMaxWidth(),
        query = ui.selectedMosque?.displayLine ?: ui.masjidId,
        onQueryChange = {
            textFieldValue = textFieldValue.copy(text = it)
            updateMasjidId.invoke(it)
        },
        onSearch = { q ->
            updateMasjidId.invoke(q)
            active = false
        },
        active = active,
        onActiveChange = { isActive ->
            requestedActive = isActive
            if (isActive) {
                previousText = textFieldValue.text
                textFieldValue = textFieldValue.copy(
                    selection = TextRange(textFieldValue.text.length)
                )
            }
            // Only expand when there are results
            active = requestedActive && ui.searchResults.isNotEmpty()
        },
        placeholder = placeholder,
        leadingIcon = {
            if (active) {
                IconButton(onClick = {
                    // restore previous text
                    textFieldValue = TextFieldValue(previousText)
                    updateMasjidId.invoke(previousText)
                    // collapse & clear focus
                    active = false
                    requestedActive = false
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        },
        trailingIcon = {
            Row {
                trailingIcon?.invoke()
                if (textFieldValue.text.isNotEmpty()) {
                    IconButton(onClick = {
                        // clear text
                        textFieldValue = TextFieldValue("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        },
    ) {
        // Results list constrained to a reasonable height to avoid unbounded constraints
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(ui.searchResults.take(10)) { mosqueDetails ->
                val slug = mosqueDetails.slug
                val title = mosqueDetails.displayLine
                ListItem(
                    headlineContent = { Text(title) },
                    supportingContent = supportingContent?.let { { it(title) } },
                    leadingContent = leadingContent,
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .clickable {
                            selectMasjidSuggestion(slug)
                            updateMasjidId(slug)
                            onOpenDetailsScreen(mosqueDetails)
                            active = false
                            requestedActive = false
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    vmUiState: PrayerUiState,
    onMasjidIdChange: (String) -> Unit,
    onAddQiyamAlarm: (QiyamAlarm) -> Unit,
    onSelectMasjidSuggestion: (String?) -> Unit,
    onComputeQiyam: (PrayerTimes?, PrayerTimes?) -> Unit,
    onModeChange: (QiyamMode) -> Unit,
    onOpenDetailsScreen: (MosqueDetails) -> Unit,
    onLogPrayed: (Boolean) -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Mawaqit Masjid ID
        item {
            val focusRequester = remember { FocusRequester() }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomizableSearchBar(
                    ui = vmUiState,
                    updateMasjidId = onMasjidIdChange,
                    selectMasjidSuggestion = onSelectMasjidSuggestion,
                    onOpenDetailsScreen = onOpenDetailsScreen,
                    modifier = Modifier.focusRequester(focusRequester)
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
                            highlight = setOf("Fajr")
                        )
                    }
                } ?: run {
                    when {
                        vmUiState.isLoading -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }

                        vmUiState.error != null -> Text(
                            text = vmUiState.error,
                            modifier = Modifier.fillMaxSize()
                        )

                        else -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Please select a mosque to view its prayer times and choose the right Qiyam mode.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Section: Qiyam (Tonight) and Helper / Education merged
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vmUiState.qiyamUiState?.let { qiyamUiState ->
                    if (vmUiState.selectedMosque != null || vmUiState.yearlyPrayers != null)
                    TonightCard(
                        qiyamUiState = qiyamUiState,
                        onAddQiyamAlarm = onAddQiyamAlarm,
                        onModeChange = onModeChange,
                        onLogPrayed = onLogPrayed
                    )

                }
            }
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