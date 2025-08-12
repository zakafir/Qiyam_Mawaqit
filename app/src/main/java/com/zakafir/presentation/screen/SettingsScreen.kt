package com.zakafir.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.zakafir.presentation.PrayerUiState
import com.zakafir.presentation.component.TimePickerField
import java.util.Locale
import kotlin.collections.plus
import kotlin.math.roundToInt


private const val MIN_NAPS = 0

@Composable
fun SettingsScreen(
    onBufferChange: (Int) -> Unit,
    onGoalChange: (Int) -> Unit,
    ui: PrayerUiState,
    onLatestMorningEndChange: (String) -> Unit,
    onDesiredSleepHoursChange: (Float) -> Unit,
    onPostFajrBufferMinChange: (Int) -> Unit,
    onIshaBufferMinChange: (Int) -> Unit,
    onMinNightStartChange: (String) -> Unit,
    onDisallowPostFajrIfFajrAfterChange: (String) -> Unit,
    onUpdateNap: (index: Int, config: NapConfig) -> Unit,
    onAddNap: () -> Unit,
    onRemoveNap: (index: Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
        Divider()
        Text(text = "Sleep Planner", style = MaterialTheme.typography.titleMedium)

        // Desired sleep (15-minute steps)
        val desiredSleepMin = (ui.desiredSleepHours * 60f).toInt()
        Text(text = "Desired sleep — ${formatHm(desiredSleepMin)}")
        Slider(
            value = desiredSleepMin.toFloat(),
            onValueChange = { raw ->
                val snapped = (raw / 15f).roundToInt() * 15 // snap to 15-minute increments
                onDesiredSleepHoursChange(snapped / 60f)
            },
            valueRange = 240f..720f // 4h..12h in minutes
        )

        // Post‑Fajr buffer
        Text(text = "Post‑Fajr buffer — ${ui.postFajrBufferMin} min")
        Slider(
            value = ui.postFajrBufferMin.toFloat(),
            onValueChange = { onPostFajrBufferMinChange(it.toInt()) },
            valueRange = 0f..120f,
            steps = 23
        )

        // Isha buffer
        Text(text = "Isha buffer — ${ui.ishaBufferMin} min")
        Slider(
            value = ui.ishaBufferMin.toFloat(),
            onValueChange = { onIshaBufferMinChange(it.toInt()) },
            valueRange = 0f..120f,
            steps = 23
        )

        // Earliest night start
        TimePickerField(
            label = "Earliest night start",
            value = ui.minNightStart,
            onValueChange = onMinNightStartChange
        )

        // Disable post‑Fajr if Fajr after
        TimePickerField(
            label = "Disable post‑Fajr sleep if Fajr after",
            value = ui.disallowPostFajrIfFajrAfter,
            onValueChange = onDisallowPostFajrIfFajrAfterChange
        )

        // Latest post‑Fajr end
        TimePickerField(
            label = "Latest post‑Fajr end",
            value = ui.latestMorningEnd,
            onValueChange = onLatestMorningEndChange
        )

        // Naps
        Divider()
        Text(text = "Naps", style = MaterialTheme.typography.titleSmall)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ui.naps.forEachIndexed { idx, nap ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Nap ${idx + 1}", style = MaterialTheme.typography.bodyMedium)
                    val canRemove = ui.naps.size > MIN_NAPS
                    TextButton(
                        onClick = { if (canRemove) onRemoveNap(idx) },
                        enabled = canRemove
                    ) { Text("Remove") }
                }
                TimePickerField(
                    label = "Start time",
                    value = nap.start,
                    onValueChange = { onUpdateNap(idx, nap.copy(start = it)) }
                )
                Text(text = "Duration — ${nap.durationMin} min")
                Slider(
                    value = nap.durationMin.toFloat(),
                    onValueChange = { onUpdateNap(idx, nap.copy(durationMin = it.toInt())) },
                    valueRange = 10f..120f,
                    steps = 22
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                val addLabel = when (ui.naps.size) {
                    0 -> "Add midday nap"
                    1 -> "Add evening nap"
                    else -> "Add nap"
                }
                Button(onClick = onAddNap) { Text(addLabel) }
            }
        }

        var desiredHours by remember { mutableStateOf(7) }
        var allowPostFajr by remember { mutableStateOf(true) }
        var latestMorningEnd by remember { mutableStateOf("07:30") }
        var postFajrBufferMin by remember { mutableStateOf(60) } // 45–60 min suggested
        var ishaBufferMin by remember { mutableStateOf(30) } // wind-down after Isha
        var naps by remember {
            mutableStateOf(
                listOf(
                    NapConfig(
                        start = "12:00",
                        durationMin = 60
                    )
                )
            )
        }
        // Seasonal constraints
        var minNightStart by remember { mutableStateOf("21:30") } // don't sleep before this
        var disallowPostFajrIfFajrAfter by remember { mutableStateOf("06:30") } // disable post‑Fajr sleep if Fajr is later than this

        val qiyamStart = ui.qiyamWindow?.start
        val todayIsha = ui.prayers?.prayerTimes?.getOrNull(0)?.icha
        val tomorrowFajr = ui.prayers?.prayerTimes?.getOrNull(1)?.fajr
        val tomorrowDhuhr = ui.prayers?.prayerTimes?.getOrNull(1)?.dohr


        SleepScheduleCard(
            desiredMinutes = desiredSleepMin,
            icha = todayIsha,
            qiyamStart = qiyamStart,
            fajr = tomorrowFajr,
            dhuhr = tomorrowDhuhr,
            allowPostFajr = ui.allowPostFajr,
            latestMorningEnd = ui.latestMorningEnd,
            postFajrBufferMin = ui.postFajrBufferMin,
            ishaBufferMin = ui.ishaBufferMin,
            naps = ui.naps,
            minNightStart = ui.minNightStart,
            disallowPostFajrIfFajrAfter = ui.disallowPostFajrIfFajrAfter
        )
    }
}

@Composable
private fun SleepScheduleCard(
    desiredMinutes: Int,
    icha: String?,
    qiyamStart: String?,
    fajr: String?,
    dhuhr: String?,
    allowPostFajr: Boolean,
    latestMorningEnd: String,
    postFajrBufferMin: Int,
    ishaBufferMin: Int,
    naps: List<NapConfig>,
    minNightStart: String,
    disallowPostFajrIfFajrAfter: String
) {
    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icha == null || qiyamStart == null) {
                Text("Sleep schedule will appear once prayer times load.")
                return@Column
            }

            val prayerBuffer = 10 // minutes to be awake before a prayer

            val blocks = mutableListOf<Pair<String, Pair<String, String>>>()

            // Isha buffer before sleeping
            val nightStartCandidate = addMinutes(icha, ishaBufferMin) ?: icha
            val nightStart = maxHm(
                nightStartCandidate,
                minNightStart
            ) // don’t sleep too early when Isha is early
            val nightBlockMin = durationBetween(nightStart, qiyamStart)
            blocks += "Night sleep" to (nightStart to qiyamStart)

            // We add blocks in priority order and CAP them so total never exceeds the target.
            var remaining = (desiredMinutes - nightBlockMin).coerceAtLeast(0)

            var postFajrMin = 0
            if (allowPostFajr && fajr != null && compareHm(
                    fajr,
                    disallowPostFajrIfFajrAfter
                ) <= 0 && remaining > 0
            ) {
                val startPost = addMinutes(fajr, postFajrBufferMin)
                val endPostLimit = latestMorningEnd
                if (startPost != null && compareHm(endPostLimit, startPost) > 0) {
                    val window = durationBetween(startPost, endPostLimit)
                    val take = minOf(window, remaining)
                    if (take > 0) {
                        postFajrMin = take
                        val cappedEnd = addMinutes(startPost, take) ?: endPostLimit
                        blocks += "Post‑Fajr sleep" to (startPost to cappedEnd)
                        remaining -= take
                    }
                }
            } else if (allowPostFajr && fajr != null) {
                blocks += "Post‑Fajr sleep disabled (Fajr after ${disallowPostFajrIfFajrAfter})" to ("—" to "—")
            }

            var napsTotalMin = 0
            if (remaining > 0) {
                naps.forEachIndexed { idx, nap ->
                    if (remaining <= 0) return@forEachIndexed
                    val start = nap.start
                    var end = addMinutes(start, nap.durationMin) ?: start
                    if (dhuhr != null && isBetween(dhuhr, start, end)) {
                        val candidate = addMinutes(dhuhr, -prayerBuffer)
                        if (candidate != null) end = candidate
                    }
                    if (compareHm(end, start) > 0 && nap.durationMin > 0) {
                        val window = durationBetween(start, end)
                        val take = minOf(window, remaining)
                        if (take > 0) {
                            napsTotalMin += take
                            val cappedEnd = addMinutes(start, take) ?: end
                            blocks += ("Nap ${idx + 1}") to (start to cappedEnd)
                            remaining -= take
                        }
                    }
                }
            }

            val totalMin = nightBlockMin + postFajrMin + napsTotalMin
            val desiredMin = desiredMinutes
            val progress = (totalMin.toFloat() / desiredMin).coerceIn(0f, 1f)

            // UI
            Text(
                text = "Notes: Waiting ${ishaBufferMin} min after Isha before going to bed. After Fajr, staying up ${postFajrBufferMin} min before any post‑Fajr sleep.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Suggested sleep ranges:")
            blocks.forEach { (label, range) ->
                Text("• $label: ${range.first} → ${range.second}")
            }

            Divider()
            Text("Total: ${formatHm(totalMin)}  /  Target: ${formatHm(desiredMin)}")
            val isUnder = totalMin < desiredMin
            LinearProgressIndicator(
                progress = progress,
                color = if (isUnder) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            )

            val deficit = desiredMin - totalMin
            if (deficit > 0) {
                Text(
                    text = "Short by ${formatHm(deficit)}. Consider: sleeping earlier after Isha, extending post‑Fajr, or using more of 12:00–14:00 before Dhuhr.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Goal met or exceeded. Keep consistent!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettings() {
    // Preview state for naps
    val (naps, setNaps) = remember {
        mutableStateOf(listOf(NapConfig(start = "12:00", durationMin = 60)))
    }
    SettingsScreen(
        onBufferChange = {},
        onGoalChange = {},
        ui = PrayerUiState(
            desiredSleepHours = 7.5f,
            postFajrBufferMin = 30,
            ishaBufferMin = 10,
            minNightStart = "21:00",
            disallowPostFajrIfFajrAfter = "06:00",
            naps = naps
        ),
        onDesiredSleepHoursChange = {},
        onPostFajrBufferMinChange = {},
        onIshaBufferMinChange = {},
        onMinNightStartChange = {},
        onDisallowPostFajrIfFajrAfterChange = {},
        onUpdateNap = { idx, config ->
            setNaps(naps.toMutableList().apply { set(idx, config) })
        },
        onAddNap = {
            setNaps(naps + NapConfig(start = "12:00", durationMin = 60))
        },
        onRemoveNap = { idx ->
            setNaps(naps.toMutableList().apply { removeAt(idx) })
        },
        onLatestMorningEndChange = {}
    )
}

private fun durationBetween(startHm: String, endHm: String): Int {
    val s = parseHm(startHm)
    val e = parseHm(endHm)
    return if (e >= s) e - s else (24 * 60 - s) + e // across midnight
}

private fun formatHm(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "${h}h ${m}m"
}

private fun addMinutes(hm: String, minutes: Int): String? {
    val base = parseHm(hm)
    val total = (base + minutes).mod(24 * 60)
    val h = total / 60
    val m = total % 60
    return String.format(Locale.US, "%02d:%02d", h, m)
}

// Returns positive if a > b, 0 if equal, negative if a < b (same day, no wrap)
private fun compareHm(a: String, b: String): Int = parseHm(a) - parseHm(b)

private fun isBetween(x: String, start: String, end: String): Boolean {
    val px = parseHm(x)
    val ps = parseHm(start)
    val pe = parseHm(end)
    return px in (ps + 1) until pe
}

private fun maxHm(a: String, b: String): String = if (compareHm(a, b) >= 0) a else b

private fun parseHm(hm: String): Int {
    // hh:mm → minutes from 00:00
    val parts = hm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h * 60 + m
}

