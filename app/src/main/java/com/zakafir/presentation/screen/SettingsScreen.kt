package com.zakafir.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakafir.domain.model.NapConfig
import com.zakafir.presentation.GlobalUiState
import com.zakafir.presentation.SettingsUiState
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class SleepBlock(val label: String, val start: String, val end: String, val minutes: Int)

@Composable
fun SettingsScreen(
    settingsUiState: SettingsUiState,
    globalUiState: GlobalUiState,
    onDesiredSleepHoursChange: (Float) -> Unit,
    onMinNightStartChange: (String) -> Unit,
) {
    // --- Pull prayer times for today/tomorrow from yearly calendar using device timezone
    val list = globalUiState.yearlyPrayers?.prayerTimes.orEmpty()
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val todayStr = todayDate.toString()
    val todayIndex = list.indexOfFirst { it.date == todayStr }

    val today = list.getOrNull(todayIndex)
    val tomorrow = list.getOrNull(todayIndex + 1)

    val todayMaghrib = today?.maghreb
    val todayIsha = today?.icha
    val qiyamStart = globalUiState.qiyamUiState?.window?.start
    val tomorrowFajr = tomorrow?.fajr
    val tomorrowDhuhr = tomorrow?.dohr
    val tomorrowAsr = tomorrow?.asr
    val tomorrowMaghrib = tomorrow?.maghreb

    // === 24h scheduling ranges (startOffset..endOffset within each window)
    var segTodayMaghIsha by remember(todayMaghrib, todayIsha) { mutableStateOf(0f..0f) }
    var segIshaQiyam by remember(todayIsha, qiyamStart) { mutableStateOf(0f..0f) }
    var segQiyamFajr by remember(qiyamStart, tomorrowFajr) { mutableStateOf(0f..0f) }
    var segFajrDhuhr by remember(tomorrowFajr, tomorrowDhuhr) { mutableStateOf(0f..0f) }
    var segMiddaySmart by remember(tomorrowDhuhr, tomorrowAsr) { mutableStateOf(0f..0f) } // 12:00–14:00 smart nap
    var segAsrMaghrib by remember(tomorrowAsr, tomorrowMaghrib) { mutableStateOf(0f..0f) }
    // --- Helpers ---
    fun smartMiddayWindow(): Pair<String?, String?> {
        val baseStart = "12:00"; val baseEnd = "14:00"; val buf = 10
        var candidates = mutableListOf<Pair<String, String>>()
        var wStart = baseStart; var wEnd = baseEnd
        // Split at Dhuhr
        if (tomorrowDhuhr != null && isBetween(tomorrowDhuhr, wStart, wEnd)) {
            val leftEnd = addMinutes(tomorrowDhuhr, -buf) ?: wStart
            val rightStart = addMinutes(tomorrowDhuhr, buf) ?: wEnd
            if (compareHm(leftEnd, wStart) > 0) candidates += wStart to leftEnd
            if (compareHm(wEnd, rightStart) > 0) candidates += rightStart to wEnd
        } else candidates += wStart to wEnd
        // Split at Asr
        if (tomorrowAsr != null) {
            val upd = mutableListOf<Pair<String, String>>()
            for ((cs, ce) in candidates) {
                if (isBetween(tomorrowAsr, cs, ce)) {
                    val leftEnd = addMinutes(tomorrowAsr, -buf) ?: cs
                    val rightStart = addMinutes(tomorrowAsr, buf) ?: ce
                    if (compareHm(leftEnd, cs) > 0) upd += cs to leftEnd
                    if (compareHm(ce, rightStart) > 0) upd += rightStart to ce
                } else upd += cs to ce
            }
            candidates = upd
        }
        val best = candidates.maxByOrNull { durationBetween(it.first, it.second) }
        return if (best != null && durationBetween(best.first, best.second) > 0) best.first to best.second else null to null
    }

    fun normalizeRangeAgainstForbidden(
        windowStart: String?, windowEnd: String?,
        selected: ClosedFloatingPointRange<Float>,
        forbiddenStartAbs: Int?, forbiddenEndAbs: Int?
    ): ClosedFloatingPointRange<Float> {
        if (windowStart == null || windowEnd == null || forbiddenStartAbs == null || forbiddenEndAbs == null) return selected
        val winLen = durationBetween(windowStart, windowEnd)
        if (winLen <= 0) return 0f..0f
        val winStartAbs = parseHm(windowStart)
        val s = (selected.start.coerceIn(0f, winLen.toFloat())).roundToInt()
        val e = (selected.endInclusive.coerceIn(0f, winLen.toFloat())).roundToInt()
        val selStartAbs = winStartAbs + s
        val selEndAbs = winStartAbs + e
        val overStart = maxOf(selStartAbs, forbiddenStartAbs)
        val overEnd = minOf(selEndAbs, forbiddenEndAbs)
        if (overEnd <= overStart) return selected // no overlap
        val leftLen = overStart - selStartAbs
        val rightLen = selEndAbs - overEnd
        return when {
            leftLen <= 0 && rightLen <= 0 -> 0f..0f
            leftLen >= rightLen -> selected.start..(selected.start + leftLen)
            else -> (selected.endInclusive - rightLen)..selected.endInclusive
        }
    }


    fun blockAbsStart(b: SleepBlock): Int {
        val dayMinutes = 24 * 60
        val l = b.label.lowercase(Locale.ROOT)
        val base = parseHm(b.start)
        val offset = if (l.startsWith("tomorrow")) dayMinutes else 0
        return base + offset
    }

    fun totalUnionMinutes(blocks: List<SleepBlock>): Int {
        if (blocks.isEmpty()) return 0
        val intervals = blocks.map { blockAbsStart(it) to (blockAbsStart(it) + it.minutes) }.sortedBy { it.first }
        var total = 0; var curStart = intervals[0].first; var curEnd = intervals[0].second
        for (i in 1 until intervals.size) {
            val (s, e) = intervals[i]
            if (s > curEnd) { total += (curEnd - curStart); curStart = s; curEnd = e } else if (e > curEnd) { curEnd = e }
        }
        total += (curEnd - curStart)
        return total
    }

    fun addSegmentWithLabel(
        label: String,
        start: String?, end: String?,
        range: ClosedFloatingPointRange<Float>, acc: MutableList<SleepBlock>
    ) {
        if (start == null || end == null) return
        val window = durationBetween(start, end)
        if (window <= 0) return
        val lo = range.start.coerceIn(0f, window.toFloat())
        val hi = range.endInclusive.coerceIn(lo, window.toFloat())
        val minutes = (hi - lo).roundToInt()
        if (minutes <= 0) return
        val s = addMinutes(start, lo.roundToInt()) ?: start
        val e = addMinutes(start, hi.roundToInt()) ?: end
        acc += SleepBlock(label, s, e, minutes)
    }

    val todayLabel = today?.date ?: todayStr
    val tomorrowLabel = tomorrow?.date ?: todayDate.plus(DatePeriod(days = 1)).toString()

    val (midSmartStart, midSmartEnd) = smartMiddayWindow()

    val plannedHeader: List<SleepBlock> = buildList {
        addSegmentWithLabel("today ($todayLabel): Maghrib → Icha", todayMaghrib, todayIsha, segTodayMaghIsha, this)
        addSegmentWithLabel("today/tomorrow ($todayLabel→$tomorrowLabel): Icha → Qiyam", todayIsha, qiyamStart, segIshaQiyam, this)
        addSegmentWithLabel("tomorrow ($tomorrowLabel): Qiyam → Fajr", qiyamStart, tomorrowFajr, segQiyamFajr, this)
        addSegmentWithLabel("tomorrow ($tomorrowLabel): Fajr → Dhuhr", tomorrowFajr, tomorrowDhuhr, segFajrDhuhr, this)
        addSegmentWithLabel("tomorrow ($tomorrowLabel): 12:00 → 14:00 (smart nap)", midSmartStart, midSmartEnd, segMiddaySmart, this)
        addSegmentWithLabel("tomorrow ($tomorrowLabel): Asr → Maghrib", tomorrowAsr, tomorrowMaghrib, segAsrMaghrib, this)
    }

    val desiredSleepMinHeader = (settingsUiState.desiredSleepHours * 60f).toInt().coerceAtLeast(0)
    val totalSleepMinHeader = totalUnionMinutes(plannedHeader)
    val progressHeader = (if (desiredSleepMinHeader == 0) 0f else totalSleepMinHeader.toFloat() / desiredSleepMinHeader).coerceIn(0f, 1f)
    val isUnderHeader = totalSleepMinHeader < desiredSleepMinHeader

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        stickyHeader {
            SleepPlannerStickyHeader(
                planned = plannedHeader,
                totalMin = totalSleepMinHeader,
                desiredMin = desiredSleepMinHeader,
                progress = progressHeader,
                isUnder = isUnderHeader
            )
        }
        item {
            Text(text = "Sleep windows", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Use the range sliders to choose exact sleep windows for the next 24 hours. Labels show today and tomorrow with dates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            DesiredSleepGoalCard(
                valueHours = settingsUiState.desiredSleepHours,
                onChange = onDesiredSleepHoursChange
            )
            SegmentSleepSlider(
                label = "today ($todayLabel): Maghrib → Icha",
                start = todayMaghrib,
                end = todayIsha,
                range = segTodayMaghIsha,
                onRangeChange = { segTodayMaghIsha = it }
            )
            SegmentSleepSlider(
                label = "today/tomorrow ($todayLabel→$tomorrowLabel): Icha → Qiyam",
                start = todayIsha,
                end = qiyamStart,
                range = segIshaQiyam,
                onRangeChange = { newRange ->
                    // Authoritative: Icha→Qiyam adjusts Qiyam→Fajr to avoid overlap
                    segIshaQiyam = newRange
                    val baseAbs = todayIsha?.let { parseHm(it) }
                    val forbStart = baseAbs?.plus(newRange.start.roundToInt())
                    val forbEnd = baseAbs?.plus(newRange.endInclusive.roundToInt())
                    segQiyamFajr = normalizeRangeAgainstForbidden(qiyamStart, tomorrowFajr, segQiyamFajr, forbStart, forbEnd)
                }
            )
            SegmentSleepSlider(
                label = "tomorrow ($tomorrowLabel): Qiyam → Fajr",
                start = qiyamStart,
                end = tomorrowFajr,
                range = segQiyamFajr,
                onRangeChange = { newRange ->
                    // Normalize against authoritative Icha→Qiyam
                    val baseAbs = todayIsha?.let { parseHm(it) }
                    val forbStart = baseAbs?.plus(segIshaQiyam.start.roundToInt())
                    val forbEnd = baseAbs?.plus(segIshaQiyam.endInclusive.roundToInt())
                    segQiyamFajr = normalizeRangeAgainstForbidden(qiyamStart, tomorrowFajr, newRange, forbStart, forbEnd)
                }
            )
            SegmentSleepSlider(
                label = "tomorrow ($tomorrowLabel): Fajr → Dhuhr",
                start = tomorrowFajr,
                end = tomorrowDhuhr,
                range = segFajrDhuhr,
                onRangeChange = { newRange ->
                    val smartStartAbs = midSmartStart?.let { parseHm(it) }
                    val forbStart = smartStartAbs?.plus(segMiddaySmart.start.roundToInt())
                    val forbEnd = smartStartAbs?.plus(segMiddaySmart.endInclusive.roundToInt())
                    segFajrDhuhr = normalizeRangeAgainstForbidden(tomorrowFajr, tomorrowDhuhr, newRange, forbStart, forbEnd)
                }
            )
            SegmentSleepSlider(
                label = "tomorrow ($tomorrowLabel): 12:00 → 14:00 (smart nap)",
                start = midSmartStart,
                end = midSmartEnd,
                range = segMiddaySmart,
                onRangeChange = { newRange ->
                    segMiddaySmart = newRange
                    val smartStartAbs = midSmartStart?.let { parseHm(it) }
                    val forbStart = smartStartAbs?.plus(newRange.start.roundToInt())
                    val forbEnd = smartStartAbs?.plus(newRange.endInclusive.roundToInt())
                    segFajrDhuhr = normalizeRangeAgainstForbidden(tomorrowFajr, tomorrowDhuhr, segFajrDhuhr, forbStart, forbEnd)
                    segAsrMaghrib = normalizeRangeAgainstForbidden(tomorrowAsr, tomorrowMaghrib, segAsrMaghrib, forbStart, forbEnd)
                }
            )
            SegmentSleepSlider(
                label = "tomorrow ($tomorrowLabel): Asr → Maghrib",
                start = tomorrowAsr,
                end = tomorrowMaghrib,
                range = segAsrMaghrib,
                onRangeChange = { newRange ->
                    val smartStartAbs = midSmartStart?.let { parseHm(it) }
                    val forbStart = smartStartAbs?.plus(segMiddaySmart.start.roundToInt())
                    val forbEnd = smartStartAbs?.plus(segMiddaySmart.endInclusive.roundToInt())
                    segAsrMaghrib = normalizeRangeAgainstForbidden(tomorrowAsr, tomorrowMaghrib, newRange, forbStart, forbEnd)
                }
            )
        }
    }
}

private fun formatHours(h: Float): String {
    val totalMin = (h * 60f).toInt()
    val hh = totalMin / 60
    val mm = totalMin % 60
    return if (mm == 0) "${hh}h" else "${hh}h ${mm}m"
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun DesiredSleepGoalCard(
    valueHours: Float,
    onChange: (Float) -> Unit
) {
    SectionCard(
        title = "Sleep goal",
        subtitle = "Set your target total sleep for the next 24 hours."
    ) {
        Text(
            text = formatHours(valueHours),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Slider(
            value = valueHours.coerceIn(3.0f, 12.0f),
            onValueChange = { onChange((Math.round(it * 4f) / 4f)) }, // snap to 15 min
            valueRange = 3.0f..12.0f,
            steps = (12 - 3) * 4 - 1
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("3h", "6h", "9h", "12h").forEach {
                Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable
private fun SleepPlannerStickyHeader(
    planned: List<SleepBlock>,
    totalMin: Int,
    desiredMin: Int,
    progress: Float,
    isUnder: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Quick summary", style = MaterialTheme.typography.titleSmall)
            SleepTimelineBar(planned = planned)
            LinearProgressIndicator(
                progress = progress,
                color = if (isUnder) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Total: ${formatHm(totalMin)} / Target: ${formatHm(desiredMin)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (planned.isNotEmpty()) {
                Text(
                    text = "Planned:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    planned.forEach {
                        Text(
                            text = "• ${it.label}: ${it.start} → ${it.end} (${formatHm(it.minutes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepTimelineBar(planned: List<SleepBlock>) {
    val dayMinutes = 24 * 60
    val barHeight = 18.dp

    fun colorFor(label: String): Color {
        val l = label.lowercase(Locale.ROOT)
        return when {
            "icha → qiyam" in l || "isha → qiyam" in l -> Color(0xFF1976D2)
            "maghrib → icha" in l || "maghrib → isha" in l -> Color(0xFF81D4FA)
            "qiyam → fajr" in l -> Color(0xFF64B5F6)
            "fajr → dhuhr" in l || "fajr → duhr" in l || "fajr → dohr" in l -> Color(0xFF4FC3F7)
            "asr → maghrib" in l -> Color(0xFF90CAF9)
            "12:00" in l -> Color(0xFF00ACC1)
            else -> Color(0xFF455A64)
        }
    }

    fun absoluteStartFor(b: SleepBlock): Int {
        val l = b.label.lowercase(Locale.ROOT)
        val base = parseHm(b.start)
        val isTomorrow = l.startsWith("tomorrow")
        return base + if (isTomorrow) dayMinutes else 0
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (dayIndex in 0..1) {
            val dayStart = dayIndex * dayMinutes
            val dayEnd = (dayIndex + 1) * dayMinutes

            val slices = planned.flatMap { b ->
                val startAbs = absoluteStartFor(b)
                val endAbs = startAbs + b.minutes
                val interStart = maxOf(startAbs, dayStart)
                val interEnd = minOf(endAbs, dayEnd)
                if (interEnd > interStart) listOf(Triple(interStart, interEnd, colorFor(b.label))) else emptyList()
            }.sortedBy { it.first }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .background(Color(0xFFEEEEEE))
            ) {
                var cursor = dayStart
                for ((s, e, c) in slices) {
                    if (s > cursor) {
                        Spacer(
                            modifier = Modifier
                                .weight((s - cursor).toFloat())
                                .fillMaxHeight()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight((e - s).toFloat())
                            .fillMaxHeight()
                            .background(c)
                    )
                    cursor = e
                }
                if (cursor < dayEnd) {
                    Spacer(
                        modifier = Modifier
                            .weight((dayEnd - cursor).toFloat())
                            .fillMaxHeight()
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("00h", "06h", "12h", "18h", "24h").forEach { Text(it, fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun SegmentSleepSlider(
    label: String,
    start: String?,
    end: String?,
    range: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (start == null || end == null) {
            Text(
                "Not available (missing prayer times)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }
        val window = durationBetween(start, end)
        val safeWindow = window.coerceAtLeast(0)
        val clampedStart = range.start.coerceIn(0f, safeWindow.toFloat())
        val clampedEnd = range.endInclusive.coerceIn(clampedStart, safeWindow.toFloat())

        val selectedStartHm = addMinutes(start, clampedStart.roundToInt()) ?: start
        val selectedEndHm = addMinutes(start, clampedEnd.roundToInt()) ?: end
        val plannedMin = (clampedEnd - clampedStart).roundToInt()

        Text(
            "Window: $start → $end  •  ${formatHm(safeWindow)}  •  Planned: ${formatHm(plannedMin)}\nSelected: $selectedStartHm → $selectedEndHm",
            style = MaterialTheme.typography.bodySmall
        )
        RangeSlider(
            value = clampedStart..clampedEnd,
            onValueChange = { onRangeChange(it) },
            valueRange = 0f..safeWindow.toFloat()
        )
        Divider()
    }
}

// === Time helpers ===
private fun parseHm(hm: String): Int {
    return try {
        val parts = hm.trim().split(":")
        val h = parts[0].toInt()
        val m = parts.getOrNull(1)?.toInt() ?: 0
        (h % 24) * 60 + (m % 60)
    } catch (_: Exception) { 0 }
}

private fun addMinutes(hm: String, minutes: Int): String? {
    val base = parseHm(hm)
    val total = (base + minutes) mod 1440
    val h = (total / 60)
    val m = (total % 60)
    return String.format(Locale.US, "%02d:%02d", h, m)
}

private infix fun Int.mod(m: Int): Int {
    val r = this % m
    return if (r < 0) r + m else r
}

private fun compareHm(a: String, b: String): Int = parseHm(a) - parseHm(b)

private fun durationBetween(start: String, end: String): Int {
    val s = parseHm(start)
    val e = parseHm(end)
    return if (e >= s) e - s else 1440 - s + e
}

// Returns true if target is in [start, end) on same day (no wrap)
private fun isBetween(target: String, startHm: String, endHm: String): Boolean {
    val t = parseHm(target); val s = parseHm(startHm); val e = parseHm(endHm)
    if (e <= s) return false
    return t in s..e
}

private fun formatHm(totalMin: Int): String {
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettings() {
    val (naps, _) = remember { mutableStateOf(listOf(NapConfig(start = "12:00", durationMin = 60))) }
    SettingsScreen(
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
        onMinNightStartChange = {},
        globalUiState = GlobalUiState()
    )
}