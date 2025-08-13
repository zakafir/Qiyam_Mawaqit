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
import androidx.compose.foundation.border
import com.zakafir.presentation.PrayerUiState
import com.zakafir.presentation.component.TimePickerField
import java.util.Locale
import kotlin.collections.plus
import kotlin.math.roundToInt


private const val MIN_NAPS = 0

/**
todo 1: store settings in the sharedPref
todo 2: take into accound maghrib and icha times, if they are too late, warn the user to take naps in that time
todo 3: the sleeping time starts after praying icha,
todo 4: if the icha time is too early, give the user the possibility to configure the prefered time to go to sleep, so that he could sleep from the configured time till the start of Qiyam
 */
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
        Text(
            text = "Tune how the app plans your night sleep, optional post‑Fajr sleep, and naps. These settings personalize your schedule based on today's Isha and tomorrow's Fajr.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = "Sleep Planner", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Core rules the planner will follow when building your sleep schedule.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Desired sleep (15-minute steps)
        val desiredSleepMin = (ui.desiredSleepHours * 60f).toInt()
        Text(text = "Desired sleep — ${formatHm(desiredSleepMin)}")
        Text(
            text = "Total sleep you aim to get in a 24‑hour period (night + naps). Drag to select, snaps to 15‑minute steps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        Text(
            text = "Time to stay awake after praying Fajr before any post‑Fajr sleep is allowed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = ui.postFajrBufferMin.toFloat(),
            onValueChange = { onPostFajrBufferMinChange(it.toInt()) },
            valueRange = 0f..120f
        )

        // Isha buffer
        Text(text = "Isha buffer — ${ui.ishaBufferMin} min")
        Text(
            text = "Wind‑down time after Isha before you are allowed to start night sleep.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = ui.ishaBufferMin.toFloat(),
            onValueChange = { onIshaBufferMinChange(it.toInt()) },
            valueRange = 0f..120f
        )

        // Preferred bedtime (not before)
        TimePickerField(
            label = "Preferred bedtime (not before)",
            value = ui.minNightStart,
            onValueChange = onMinNightStartChange
        )
        Text(
            text = "You prefer to go to bed at this time (e.g., 22:00) or later. • If Isha is earlier than this, you still wait until this time. • If Isha is later than this, the planner may schedule a short nap before Isha and wake you up for prayer.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Disable post‑Fajr if Fajr after
        TimePickerField(
            label = "Disable post‑Fajr sleep if Fajr after",
            value = ui.disallowPostFajrIfFajrAfter,
            onValueChange = onDisallowPostFajrIfFajrAfterChange
        )
        Text(
            text = "If tomorrow's Fajr is later than this time, the planner will skip the post‑Fajr sleep block.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Latest post‑Fajr end
        TimePickerField(
            label = "Latest post‑Fajr end",
            value = ui.latestMorningEnd,
            onValueChange = onLatestMorningEndChange
        )
        Text(
            text = "Cut‑off time for any post‑Fajr sleep. The planner won't schedule sleep after this time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Naps
        Divider()
        Text(text = "Naps", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "Optional extra sleep blocks during the day. The planner will use them only if needed to reach your total sleep goal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    valueRange = 10f..120f
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

        val qiyamStart = ui.qiyamUiState?.window?.start
        val todayIsha = ui.yearlyPrayers?.prayerTimes?.getOrNull(0)?.icha
        val todayMaghrib = ui.yearlyPrayers?.prayerTimes?.getOrNull(0)?.maghreb
        val tomorrowFajr = ui.yearlyPrayers?.prayerTimes?.getOrNull(1)?.fajr
        val tomorrowDhuhr = ui.yearlyPrayers?.prayerTimes?.getOrNull(1)?.dohr

        Divider()
        Text(text = "Sleep plan preview", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "A live preview of tonight’s plan based on your settings and prayer times.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SleepScheduleCard(
            desiredMinutes = desiredSleepMin,
            icha = todayIsha,
            maghrib = todayMaghrib,
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
    maghrib: String?,
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CardDefaults.elevatedShape),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Tonight’s sleep plan",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Preview updates automatically when you change settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (icha == null || qiyamStart == null) {
                Text("Sleep schedule will appear once prayer times load.")
                return@Column
            }

            val prayerBuffer = 10 // minutes to be awake before a prayer

            val blocks = mutableListOf<Pair<String, Pair<String, String>>>()

            // Optional pre‑Isha nap if Isha is later than preferred bedtime.
            // Starts at preferred bedtime (minNightStart) and ends before Maghrib/Isha by a small buffer.
            var preIshaNapMin = 0
            if (icha != null && compareHm(icha, minNightStart) > 0) {
                val napStart = minNightStart
                // End before Isha by buffer
                var napEnd = addMinutes(icha, -prayerBuffer) ?: icha
                // If Maghrib falls inside, end before Maghrib by buffer
                if (maghrib != null && isBetween(maghrib, napStart, napEnd)) {
                    val beforeMaghrib = addMinutes(maghrib, -prayerBuffer)
                    if (beforeMaghrib != null) {
                        napEnd = minHm(napEnd, beforeMaghrib)
                    }
                }
                if (compareHm(napEnd, napStart) > 0) {
                    val window = durationBetween(napStart, napEnd)
                    val take = minOf(window, desiredMinutes) // nap is capped by the remaining later
                    if (take > 0) {
                        preIshaNapMin = take
                        val cappedEnd = addMinutes(napStart, take) ?: napEnd
                        // Insert the pre‑Isha nap as the first block
                        blocks += "Pre‑Isha nap" to (napStart to cappedEnd)
                    }
                }
            }

            // Isha buffer before sleeping
            val nightStartCandidate = addMinutes(icha, ishaBufferMin) ?: icha
            val nightStart = maxHm(
                nightStartCandidate,
                minNightStart
            ) // don’t sleep too early when Isha is early
            val nightBlockMin = durationBetween(nightStart, qiyamStart)
            blocks += "Night sleep" to (nightStart to qiyamStart)

            // We add blocks in priority order and CAP them so total never exceeds the target.
            var remaining = (desiredMinutes - preIshaNapMin - nightBlockMin).coerceAtLeast(0)

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
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = "Goal met or exceeded. Keep consistent!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
            postFajrBufferMin = 30,
            ishaBufferMin = 10,
            minNightStart = "21:00",
            disallowPostFajrIfFajrAfter = "06:00",
            naps = naps,
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
private fun minHm(a: String, b: String): String = if (compareHm(a, b) <= 0) a else b

private fun parseHm(hm: String): Int {
    // hh:mm → minutes from 00:00
    val parts = hm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h * 60 + m
}

