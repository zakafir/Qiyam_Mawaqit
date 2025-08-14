package com.zakafir.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import com.zakafir.presentation.GlobalUiState
import com.zakafir.presentation.component.TimePickerField
import java.util.Locale
import kotlin.collections.plus
import kotlin.math.roundToInt
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.zakafir.domain.model.NapConfig
import com.zakafir.presentation.SettingsUiState


private const val MIN_NAPS = 0

@Composable
fun SettingsScreen(
    globalUiState: GlobalUiState,
    settingsUiState: SettingsUiState,
    onLatestMorningEndChange: (String) -> Unit,
    onDesiredSleepHoursChange: (Float) -> Unit,
    onPostFajrBufferMinChange: (Int) -> Unit,
    onIshaBufferMinChange: (Int) -> Unit,
    onMinNightStartChange: (String) -> Unit,
    onDisallowPostFajrIfFajrAfterChange: (String) -> Unit,
    onUpdateNap: (index: Int, config: NapConfig) -> Unit,
    onAddNap: () -> Unit,
    onRemoveNap: (index: Int) -> Unit,
    onEnableIshaBufferChange: (Boolean) -> Unit,
    onEnablePostFajrChange: (Boolean) -> Unit,
    onEnableNapsChange: (Boolean) -> Unit,
    // Work & commute (provided by VM)
    onWorkStartChange: (String) -> Unit,
    onWorkEndChange: (String) -> Unit,
    onCommuteToMinChange: (Int) -> Unit,
    onCommuteFromMinChange: (Int) -> Unit,
    onSaveSettings: () -> Unit,
    onResetDefaults: () -> Unit,
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
        // Quick summary
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Quick summary", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = buildString {
                        append("Isha buffer: ")
                        append(if (settingsUiState.enableIshaBuffer) "ON (${settingsUiState.ishaBufferMin} min)" else "OFF")
                        append(" • Post‑Fajr: ")
                        append(if (settingsUiState.enablePostFajr) "ON (${settingsUiState.postFajrBufferMin} min buffer, latest ${settingsUiState.latestMorningEnd})" else "OFF")
                        append(" • Naps: ")
                        append(if (settingsUiState.enableNaps) "${settingsUiState.naps.size} configured" else "OFF")
                        append("\nDesired sleep: ")
                        append(formatHm((settingsUiState.desiredSleepHours * 60).toInt()))
                        append(" • Preferred bedtime ≥ ")
                        append(settingsUiState.minNightStart)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onSaveSettings) { Text("Save settings") }
                    TextButton(onClick = onResetDefaults) { Text("Reset defaults") }
                }
                Text(
                    text = "Tip: Changes are applied immediately. Use ‘Save settings’ to persist to storage (SharedPreferences via VM).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))


        // Tonigh's sleep time
        // Desired sleep (15-minute steps)
        val desiredSleepMin = (settingsUiState.desiredSleepHours * 60f).toInt()

        val qiyamStart = globalUiState.qiyamUiState?.window?.start
        val todayIsha = globalUiState.yearlyPrayers?.prayerTimes?.getOrNull(0)?.icha
        val todayMaghrib = globalUiState.yearlyPrayers?.prayerTimes?.getOrNull(0)?.maghreb
        val tomorrowFajr = globalUiState.yearlyPrayers?.prayerTimes?.getOrNull(1)?.fajr
        val tomorrowDhuhr = globalUiState.yearlyPrayers?.prayerTimes?.getOrNull(1)?.dohr

        // Warnings if Maghrib/Isha are late relative to preferred bedtime
        val ishaLateMinutes =
            if (todayIsha != null) compareHm(todayIsha, settingsUiState.minNightStart) else 0
        val maghribLate = (todayMaghrib != null) && compareHm(todayMaghrib, "22:00") >= 0
        if (todayIsha != null && ishaLateMinutes > 60) {
            Text(
                text = "Isha is significantly later than your preferred bedtime (>${ishaLateMinutes} min). Consider a short pre‑Isha nap.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (maghribLate) {
            Text(
                text = "Maghrib is late tonight. Plan your evening wind‑down accordingly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        SleepScheduleCard(
            desiredMinutes = desiredSleepMin,
            todayIsha = todayIsha,
            todayMaghrib = todayMaghrib,
            qiyamStart = qiyamStart,
            tomorrowFajr = tomorrowFajr,
            tomorrowDhuhr = tomorrowDhuhr,
            allowPostFajr = settingsUiState.allowPostFajr && settingsUiState.enablePostFajr,
            latestMorningEnd = settingsUiState.latestMorningEnd,
            postFajrBufferMin = if (settingsUiState.enablePostFajr) settingsUiState.postFajrBufferMin else 0,
            ishaBufferMin = if (settingsUiState.enableIshaBuffer) settingsUiState.ishaBufferMin else 0,
            naps = if (settingsUiState.enableNaps) settingsUiState.naps else emptyList(),
            minNightStart = settingsUiState.minNightStart,
            disallowPostFajrIfFajrAfter = settingsUiState.disallowPostFajrIfFajrAfter,
            desiredSleepMin = desiredSleepMin,
            globalUiState = globalUiState,
            settingsUiState = settingsUiState,
        )


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
        Text(
            text = "Night sleep starts after Isha (plus optional buffer). If Isha is earlier than your preferred bedtime, sleep begins at your preferred bedtime.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Enable Isha buffer")
            Switch(
                checked = settingsUiState.enableIshaBuffer,
                onCheckedChange = onEnableIshaBufferChange,
                colors = SwitchDefaults.colors()
            )
        }
        // Isha buffer
        if (settingsUiState.enableIshaBuffer) {
            Text(text = "Isha buffer — ${settingsUiState.ishaBufferMin} min")
            Text(
                text = "Wind‑down time after Isha before you are allowed to start night sleep.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = settingsUiState.ishaBufferMin.toFloat(),
                onValueChange = {
                    val v = it.toInt()
                    onIshaBufferMinChange(v)
                },
                valueRange = 0f..120f
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Enable post‑Fajr sleep")
            Switch(
                checked = settingsUiState.enablePostFajr,
                onCheckedChange = onEnablePostFajrChange
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Enable naps")
            Switch(
                checked = settingsUiState.enableNaps,
                onCheckedChange = onEnableNapsChange
            )
        }
        // Naps
        Divider()
        Text(text = "Naps", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "Optional extra sleep blocks during the day. The planner will use them only if needed to reach your total sleep goal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (settingsUiState.enableNaps) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                settingsUiState.naps.forEachIndexed { idx, nap ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Nap ${idx + 1}", style = MaterialTheme.typography.bodyMedium)
                        val canRemove = settingsUiState.naps.size > MIN_NAPS
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
                    val addLabel = when (settingsUiState.naps.size) {
                        0 -> "Add midday nap"
                        1 -> "Add evening nap"
                        else -> "Add nap"
                    }
                    Button(onClick = onAddNap) { Text(addLabel) }
                }
            }
        } else {
            Text(
                text = "Naps are disabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
        if (settingsUiState.enablePostFajr) {
            Text(text = "Post‑Fajr buffer — ${settingsUiState.postFajrBufferMin} min")
            Text(
                text = "Time to stay awake after praying Fajr before any post‑Fajr sleep is allowed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = settingsUiState.postFajrBufferMin.toFloat(),
                onValueChange = {
                    val v = it.toInt()
                    onPostFajrBufferMinChange(v)
                },
                valueRange = 0f..120f
            )
        }

        // Work & Commute
        Divider()
        Text(text = "Work & Commute", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Your fixed daytime obligations. These will be considered in the daily time allocation preview.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                TimePickerField(
                    label = "Work starts",
                    value = settingsUiState.workStart,
                    onValueChange = onWorkStartChange
                )
            }
            Column(Modifier.weight(1f)) {
                TimePickerField(
                    label = "Work ends",
                    value = settingsUiState.workEnd,
                    onValueChange = onWorkEndChange
                )
            }
        }
        Text(text = "Commute to work — ${settingsUiState.commuteToMin} min")
        Slider(
            value = settingsUiState.commuteToMin.toFloat(),
            onValueChange = { onCommuteToMinChange(it.toInt()) },
            valueRange = 0f..180f
        )
        Text(text = "Commute from work — ${settingsUiState.commuteFromMin} min")
        Slider(
            value = settingsUiState.commuteFromMin.toFloat(),
            onValueChange = { onCommuteFromMinChange(it.toInt()) },
            valueRange = 0f..180f
        )

        // Preferred bedtime (not before)
        TimePickerField(
            label = "Preferred bedtime (not before)",
            value = settingsUiState.minNightStart,
            onValueChange = { onMinNightStartChange(it) }
        )
        Text(
            text = "You prefer to go to bed at this time (e.g., 22:00) or later. • If Isha is earlier than this, you still wait until this time. • If Isha is later than this, the planner may schedule a short nap before Isha and wake you up for prayer.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Disable post‑Fajr if Fajr after
        TimePickerField(
            label = "Disable post‑Fajr sleep if Fajr after",
            value = settingsUiState.disallowPostFajrIfFajrAfter,
            onValueChange = { onDisallowPostFajrIfFajrAfterChange(it) }
        )
        Text(
            text = "If tomorrow's Fajr is later than this time, the planner will skip the post‑Fajr sleep block.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Latest post‑Fajr end
        TimePickerField(
            label = "Latest post‑Fajr end",
            value = settingsUiState.latestMorningEnd,
            onValueChange = { onLatestMorningEndChange(it) }
        )
        Text(
            text = "Cut‑off time for any post‑Fajr sleep. The planner won't schedule sleep after this time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

    }
}


@Composable fun SleepPlanPreview(
    globalUiState: GlobalUiState,
    settingsUiState: SettingsUiState,
    qiyamStart: String?,
    todayIsha: String?,
    todayMaghrib: String?,
    tomorrowFajr: String?,
    tomorrowDhuhr: String?,
    desiredSleepMin: Int
) {
    Divider()
    Text(text = "Sleep plan preview", style = MaterialTheme.typography.titleMedium)
    Text(
        text = "A live preview of tonight’s plan based on your settings and prayer times.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    // Compute target sleep bar logic (mirror SleepScheduleCard's totalMin logic)
    val prayerBufferForPreviews = 10 // minutes to be awake before a prayer

    // Night sleep: from max(Isha+buffer, minNightStart) to qiyamStart
    val ishaWithBuffer = todayIsha?.let { addMinutes(it, settingsUiState.ishaBufferMin) }
        ?: settingsUiState.minNightStart
    val nightStartBar = maxHm(ishaWithBuffer, settingsUiState.minNightStart)
    val nightBlockMinBar =
        if (qiyamStart != null) durationBetween(nightStartBar, qiyamStart) else 0

    // Optional pre‑Isha nap if Isha is later than preferred bedtime
    var preIshaNapMinBar = 0
    if (todayIsha != null && compareHm(todayIsha, settingsUiState.minNightStart) > 0) {
        val napStart = settingsUiState.minNightStart
        var napEnd = addMinutes(todayIsha, -prayerBufferForPreviews) ?: todayIsha
        if (todayMaghrib != null && isBetween(todayMaghrib, napStart, napEnd)) {
            val beforeMaghrib = addMinutes(todayMaghrib, -prayerBufferForPreviews)
            if (beforeMaghrib != null) {
                napEnd = minHm(napEnd, beforeMaghrib)
            }
        }
        if (compareHm(napEnd, napStart) > 0) {
            val window = durationBetween(napStart, napEnd)
            val take = minOf(window, desiredSleepMin) // will be capped by remaining below
            if (take > 0) preIshaNapMinBar = take
        }
    }

    // Remaining target after pre‑Isha and night sleep
    var remainingBar = (desiredSleepMin - preIshaNapMinBar - nightBlockMinBar).coerceAtLeast(0)

    // Post‑Fajr sleep: only if allowed and Fajr before cutoff, capped by remaining
    var postFajrMinBar = 0
    if (
        settingsUiState.allowPostFajr && settingsUiState.enablePostFajr && tomorrowFajr != null &&
        compareHm(
            tomorrowFajr,
            settingsUiState.disallowPostFajrIfFajrAfter
        ) <= 0 && remainingBar > 0
    ) {
        val postStart = addMinutes(tomorrowFajr, settingsUiState.postFajrBufferMin)
        if (postStart != null && compareHm(settingsUiState.latestMorningEnd, postStart) > 0) {
            val window = durationBetween(postStart, settingsUiState.latestMorningEnd)
            val take = minOf(window, remainingBar)
            if (take > 0) {
                postFajrMinBar = take
                remainingBar -= take
            }
        }
    }

    // Naps (only if enabled), each capped by remaining target; respect Dhuhr overlap like in card
    var napsTotalBar = 0
    if (settingsUiState.enableNaps && remainingBar > 0) {
        settingsUiState.naps.forEach { nap ->
            if (remainingBar <= 0) return@forEach
            val start = nap.start
            var end = addMinutes(start, nap.durationMin) ?: start
            if (tomorrowDhuhr != null && isBetween(tomorrowDhuhr, start, end)) {
                val cut = addMinutes(tomorrowDhuhr, -prayerBufferForPreviews)
                if (cut != null) end = cut
            }
            if (compareHm(end, start) > 0) {
                val window = durationBetween(start, end)
                val take = minOf(window, remainingBar)
                if (take > 0) {
                    napsTotalBar += take
                    remainingBar -= take
                }
            }
        }
    }

    val totalSleepBar =
        (preIshaNapMinBar + nightBlockMinBar + postFajrMinBar + napsTotalBar).coerceAtLeast(0)

    // Daily time allocation (Sleep vs Qiyam vs Work+Commute)
    val qiyamEnd = globalUiState.qiyamUiState?.window?.end
    val qiyamMin =
        if (qiyamStart != null && qiyamEnd != null) durationBetween(qiyamStart, qiyamEnd) else 0
    val workMin = durationBetween(settingsUiState.workStart, settingsUiState.workEnd)
    val workPlusCommuteMin =
        (workMin + settingsUiState.commuteToMin + settingsUiState.commuteFromMin).coerceAtLeast(
            0
        )

    StackedDailyBar(
        sleepMin = totalSleepBar,
        qiyamMin = qiyamMin,
        workCommuteMin = workPlusCommuteMin,
    )

    // Suggestions under the bar
    val suggestions = buildList {
        if (qiyamStart != null) add("Night sleep: ${nightStartBar} → ${qiyamStart}")
        if (postFajrMinBar > 0 && tomorrowFajr != null) {
            val startPost =
                addMinutes(tomorrowFajr, settingsUiState.postFajrBufferMin) ?: tomorrowFajr
            val endPost = settingsUiState.latestMorningEnd
            add("Post‑Fajr: ${startPost} → ${endPost}")
        }
        if (settingsUiState.enableNaps && settingsUiState.naps.isNotEmpty()) {
            settingsUiState.naps.forEachIndexed { idx, n ->
                val napEnd = addMinutes(n.start, n.durationMin) ?: n.start
                add("Nap ${idx + 1}: ${n.start} → ${napEnd}")
            }
        }
        add("Work: ${settingsUiState.workStart} → ${settingsUiState.workEnd} (Commute ${settingsUiState.commuteToMin}+${settingsUiState.commuteFromMin} min)")
    }
    if (suggestions.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Suggestions:\n" + suggestions.joinToString("\n") { "• " + it },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Text(
        text = "The preview shows suggested ranges. Actual scheduling respects prayer buffers and your preferred bedtime.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
@Composable
private fun StackedDailyBar(
    sleepMin: Int,
    qiyamMin: Int,
    workCommuteMin: Int,
) {
    val total = 24 * 60
    val s = sleepMin.coerceAtLeast(0)
    val q = qiyamMin.coerceAtLeast(0)
    val w = workCommuteMin.coerceAtLeast(0)
    val used = (s + q + w).coerceAtMost(total)
    val free = (total - used).coerceAtLeast(0)

    val sleepColor = Color(0xFF4CAF50)     // green
    val qiyamColor = Color(0xFFFFC107)     // amber
    val workColor = Color(0xFF2196F3)      // blue
    val freeColor = Color(0xFF9E9E9E)      // grey

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Legend
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendItem(color = sleepColor, label = "Sleep")
            LegendItem(color = qiyamColor, label = "Qiyam")
            LegendItem(color = workColor, label = "Work+Commute")
            LegendItem(color = freeColor, label = "Free")
        }
        Text(text = "Daily time allocation (24h)")
        Row(Modifier
            .fillMaxWidth()
            .height(12.dp)) {
            if (s > 0) Box(
                Modifier
                    .weight(s.toFloat(), fill = true)
                    .fillMaxHeight()
                    .background(sleepColor)
            )
            if (q > 0) Box(
                Modifier
                    .weight(q.toFloat(), fill = true)
                    .fillMaxHeight()
                    .background(qiyamColor)
            )
            if (w > 0) Box(
                Modifier
                    .weight(w.toFloat(), fill = true)
                    .fillMaxHeight()
                    .background(workColor)
            )
            if (free > 0) Box(
                Modifier
                    .weight(free.toFloat(), fill = true)
                    .fillMaxHeight()
                    .background(freeColor)
            )
        }
        Text(
            text = "Sleep: ${formatHm(s)}  •  Qiyam: ${formatHm(q)}  •  Work+Commute: ${formatHm(w)}  •  Free: ${
                formatHm(
                    free
                )
            }",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier
            .size(12.dp)
            .background(color))
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SleepScheduleCard(
    desiredMinutes: Int,
    todayIsha: String?,
    todayMaghrib: String?,
    qiyamStart: String?,
    tomorrowFajr: String?,
    tomorrowDhuhr: String?,
    allowPostFajr: Boolean,
    latestMorningEnd: String,
    postFajrBufferMin: Int,
    ishaBufferMin: Int,
    naps: List<NapConfig>,
    minNightStart: String,
    disallowPostFajrIfFajrAfter: String,
    desiredSleepMin: Int,
    globalUiState: GlobalUiState,
    settingsUiState: SettingsUiState,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                CardDefaults.elevatedShape
            ),
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
            if (todayIsha == null || qiyamStart == null) {
                Text("Sleep schedule will appear once prayer times load.")
                return@Column
            }

            val prayerBuffer = 10 // minutes to be awake before a prayer

            val blocks = mutableListOf<Pair<String, Pair<String, String>>>()

            // Optional pre‑Isha nap if Isha is later than preferred bedtime.
            // Starts at preferred bedtime (minNightStart) and ends before Maghrib/Isha by a small buffer.
            var preIshaNapMin = 0
            if (todayIsha != null && compareHm(todayIsha, minNightStart) > 0) {
                val napStart = minNightStart
                // End before Isha by buffer
                var napEnd = addMinutes(todayIsha, -prayerBuffer) ?: todayIsha
                // If Maghrib falls inside, end before Maghrib by buffer
                if (todayMaghrib != null && isBetween(todayMaghrib, napStart, napEnd)) {
                    val beforeMaghrib = addMinutes(todayMaghrib, -prayerBuffer)
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
            val nightStartCandidate = addMinutes(todayIsha, ishaBufferMin) ?: todayIsha
            val nightStart = maxHm(
                nightStartCandidate,
                minNightStart
            ) // don’t sleep too early when Isha is early
            val nightBlockMin = durationBetween(nightStart, qiyamStart)
            blocks += "Night sleep" to (nightStart to qiyamStart)

            // We add blocks in priority order and CAP them so total never exceeds the target.
            var remaining = (desiredMinutes - preIshaNapMin - nightBlockMin).coerceAtLeast(0)

            var postFajrMin = 0
            if (allowPostFajr && tomorrowFajr != null && compareHm(
                    tomorrowFajr,
                    disallowPostFajrIfFajrAfter
                ) <= 0 && remaining > 0
            ) {
                val startPost = addMinutes(tomorrowFajr, postFajrBufferMin)
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
            } else if (allowPostFajr && tomorrowFajr != null) {
                blocks += "Post‑Fajr sleep disabled (Fajr after ${disallowPostFajrIfFajrAfter})" to ("—" to "—")
            }

            var napsTotalMin = 0
            if (remaining > 0) {
                naps.forEachIndexed { idx, nap ->
                    if (remaining <= 0) return@forEachIndexed
                    val start = nap.start
                    var end = addMinutes(start, nap.durationMin) ?: start
                    if (tomorrowDhuhr != null && isBetween(tomorrowDhuhr, start, end)) {
                        val candidate = addMinutes(tomorrowDhuhr, -prayerBuffer)
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

        SleepPlanPreview(
            globalUiState = globalUiState,
            settingsUiState = settingsUiState,
            qiyamStart = qiyamStart,
            todayIsha = todayIsha,
            todayMaghrib = todayMaghrib,
            tomorrowFajr = tomorrowFajr,
            tomorrowDhuhr = tomorrowDhuhr,
            desiredSleepMin = desiredSleepMin
        )
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
        globalUiState = GlobalUiState(),
        settingsUiState = SettingsUiState(
            postFajrBufferMin = 30,
            ishaBufferMin = 10,
            minNightStart = "21:00",
            disallowPostFajrIfFajrAfter = "06:00",
            naps = naps,
            workStart = "06:00",
            workEnd = "14:00",
            commuteToMin = 0,
            commuteFromMin = 0
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
        onLatestMorningEndChange = {

        },
        onEnableIshaBufferChange = {

        },
        onEnablePostFajrChange = {

        },
        onEnableNapsChange = {

        },
        onWorkStartChange = {},
        onWorkEndChange = {},
        onCommuteToMinChange = {},
        onCommuteFromMinChange = {},
        onSaveSettings = {},
        onResetDefaults = {}
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

