package com.zakafir.data.local

import android.content.Context
import android.content.SharedPreferences
import com.zakafir.data.mapper.toData
import com.zakafir.data.mapper.toDomain
import com.zakafir.domain.datasource.LocalDataSource
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.domain.model.YearlyPrayers
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import androidx.core.content.edit
import com.zakafir.data.model.NapConfigDTO
import com.zakafir.data.model.QiyamLogDTO
import com.zakafir.domain.model.NapConfig
import com.zakafir.domain.model.QiyamLog
import com.zakafir.domain.model.QiyamMode
import java.util.Calendar
import kotlin.collections.toMutableList

class LocalDataSourceImpl(
    private val context: Context,
    private val prefs: SharedPreferences,
) : LocalDataSource {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val QIYAM_HISTORY_KEY = "qiyam_history"

    override fun readYearlyPrayers(masjidId: String): YearlyPrayers? {
        val file = File(context.filesDir, "$masjidId.json")
        if (!file.exists() || file.length() == 0L) return null
        return runCatching {
            val text = file.readText()
            json.decodeFromString<YearlyPrayers>(text)
        }.getOrNull()
    }

    override fun writeYearlyPrayers(
        masjidId: String,
        yearly: YearlyPrayers
    ) {
        val dto = yearly.toData()
        val text = runCatching { json.encodeToString(dto) }.getOrElse { throw it }
        context.openFileOutput("$masjidId.json", Context.MODE_PRIVATE).use { out ->
            out.write(text.toByteArray())
        }
        prefs.edit { putString("selected_masjid_id", masjidId) }
    }

    override fun computeQiyamWindow(
        mode: QiyamMode,
        todaysPrayerTimes: PrayerTimes?,
        tommorowsPrayerTimes: PrayerTimes?
    ): Result<QiyamWindow> {
        return runCatching {
            requireNotNull(todaysPrayerTimes) { "Today's prayer times are null" }
            requireNotNull(tommorowsPrayerTimes) { "Tomorrow's prayer times are null" }
            // Helpers
            fun parseMinutes(hhmm: String): Int {
                val parts = hhmm.trim().split(":")
                require(parts.size == 2) { "Invalid time format: $hhmm" }
                val h = parts[0].toInt()
                val m = parts[1].toInt()
                require(h in 0..23 && m in 0..59) { "Invalid time value: $hhmm" }
                return h * 60 + m
            }

            fun formatMinutes(minutes: Int): String {
                val total = ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60) // wrap safely
                val h = total / 60
                val m = total % 60
                return "%02d:%02d".format(h, m)
            }

            // Extract needed times (all in minutes since today's midnight)
            val maghribToday = parseMinutes(todaysPrayerTimes.maghreb)
            // JSON and domain use "icha" for isha
            val ishaToday = parseMinutes(todaysPrayerTimes.icha)
            val fajrTomorrowAbs = 24 * 60 + parseMinutes(tommorowsPrayerTimes.fajr)

            // Compute the full night length from Maghrib (today) to Fajr (tomorrow)
            val nightLength = fajrTomorrowAbs - maghribToday
            require(nightLength > 0) { "Non-positive night length computed." }

            val (startAbs, endAbs) = when (mode) {
                is QiyamMode.AfterIsha -> {
                    val s = ishaToday
                    val e = fajrTomorrowAbs
                    s to e
                }

                is QiyamMode.LastHalf -> {
                    val half = maghribToday + (nightLength / 2)
                    val s = maxOf(half, ishaToday) // never before Isha
                    val e = fajrTomorrowAbs
                    s to e
                }

                is QiyamMode.LastThird -> {
                    val lastThirdStartAbs = fajrTomorrowAbs - (nightLength / 3)
                    val s = maxOf(lastThirdStartAbs, ishaToday) // never before Isha
                    val e = fajrTomorrowAbs
                    s to e
                }

                is QiyamMode.Dawud -> {
                    // Dawud: middle third of the night (4th–5th sixths): [Maghrib + 1/2 L, Maghrib + 5/6 L], then clamp to [Isha, Fajr].
                    val half = maghribToday + (nightLength / 2)
                    val fiveSixths = maghribToday + (nightLength * 5) / 6
                    val s = maxOf(half, ishaToday)
                    val e = minOf(fiveSixths, fajrTomorrowAbs)
                    s to e
                }
            }

            QiyamWindow(
                start = formatMinutes(startAbs),
                end = formatMinutes(endAbs)
            )
        }
    }

    override fun getLastSelectedMasjidId(): String? {
        return prefs.getString("selected_masjid_id", null)
    }

    override fun saveLastSelectedMasjidId(masjidId: String) {
        prefs.edit { putString("selected_masjid_id", masjidId) }
    }

    override fun saveLastSelectedMasjidName(masjidId: String) {
        prefs.edit { putString("selected_masjid_name", masjidId) }
    }

    override fun updatePostFajrBuffer(v: Int) {
        setPostFajrBufferMin(v.coerceAtLeast(0))
    }

    override fun updateIshaBuffer(v: Int) {
        setIshaBufferMin(v.coerceAtLeast(0))
    }

    override fun updateMinNightStart(v: String) {
        setMinNightStart(v)
    }

    override fun updatePostFajrCutoff(v: String) {
        setDisallowPostFajrIfFajrAfter(v)
    }

    override fun updateNap(index: Int, config: NapConfig) {
        val serialized = prefs.getString("naps_serialized", null)

        val current = if (serialized.isNullOrBlank()) {
            mutableListOf<NapConfigDTO>()
        } else {
            runCatching {
                json.decodeFromString<List<NapConfigDTO>>(serialized).toMutableList()
            }.getOrElse { mutableListOf() }
        }

        if (index in current.indices) {
            current[index] = config.toData() // store as DATA DTO
            setNapsSerialized(json.encodeToString(current))
        }
    }

    override fun addNap() {
        val serialized = prefs.getString("naps_serialized", null)
        val current = if (serialized.isNullOrBlank()) mutableListOf<NapConfigDTO>() else runCatching {
            json.decodeFromString<List<NapConfigDTO>>(serialized).toMutableList()
        }.getOrElse { mutableListOf() }
        if (current.size < 3) {
            current.add(NapConfigDTO(start = "00:00", durationMin = 0))
            val out = json.encodeToString(current)
            setNapsSerialized(out)
        }
    }

    override fun updateDesiredSleepHours(v: Float) {
        val minutes = (v * 60f).toInt().coerceIn(240, 720)
        setDesiredSleepMinutes(minutes)
    }

    override fun updateBufferMinutes(v: Int) {
        prefs.edit { putInt("buffer_minutes", v.coerceAtLeast(0)) }
    }

    override fun updateAllowPostFajr(allow: Boolean) {
        prefs.edit { putBoolean("allow_post_fajr", allow) }
    }

    override fun updateLatestMorningEnd(v: String) {
        setLatestMorningEnd(v)
    }

    override fun removeNap(index: Int) {
        val serialized = prefs.getString("naps_serialized", null)
        val current = if (serialized.isNullOrBlank()) mutableListOf<NapConfigDTO>() else runCatching {
            json.decodeFromString<List<NapConfigDTO>>(serialized).toMutableList()
        }.getOrElse { mutableListOf() }
        if (index in current.indices) {
            current.removeAt(index)
            val out = json.encodeToString(current)
            setNapsSerialized(out)
        }
    }

    override fun enableNaps(enabled: Boolean) {
        prefs.edit { putBoolean("enable_naps", enabled) }
    }

    override fun enablePostFajr(enabled: Boolean) {
        prefs.edit { putBoolean("enable_post_fajr", enabled) }
    }

    override fun enableIshaBuffer(enabled: Boolean) {
        prefs.edit { putBoolean("enable_isha_buffer", enabled) }
    }

    override fun updateWorkEnd(it: String) {
        prefs.edit { putString("work_end", it) }
    }

    override fun updateWorkStart(it: String) {
        prefs.edit { putString("work_start", it) }
    }

    override fun updateCommuteToMin(it: Int) {
        prefs.edit { putInt("commute_to_min", it.coerceAtLeast(0)) }
    }

    override fun updateCommuteFromMin(it: Int) {
        prefs.edit { putInt("commute_from_min", it.coerceAtLeast(0)) }
    }

    override fun setDesiredSleepMinutes(minutes: Int) {
        prefs.edit { putInt("desired_sleep_min", minutes.coerceIn(240, 720)) }
    }

    override fun setPostFajrBufferMin(minutes: Int) {
        prefs.edit { putInt("post_fajr_buffer_min", minutes.coerceAtLeast(0)) }
    }

    override fun setIshaBufferMin(minutes: Int) {
        prefs.edit { putInt("isha_buffer_min", minutes.coerceAtLeast(0)) }
    }

    override fun setMinNightStart(hhmm: String) {
        prefs.edit { putString("min_night_start", hhmm) }
    }

    override fun setDisallowPostFajrIfFajrAfter(hhmm: String) {
        prefs.edit { putString("disallow_post_fajr_if_after", hhmm) }
    }

    override fun setLatestMorningEnd(hhmm: String) {
        prefs.edit { putString("latest_morning_end", hhmm) }
    }

    override fun setCommuteFromMin(minutes: Int) {
        prefs.edit { putInt("commute_from_min", minutes.coerceAtLeast(0)) }
    }

    override fun setNapsSerialized(serialized: String) {
        prefs.edit { putString("naps_serialized", serialized) }
    }

    override fun logQiyam(date: String, prayed: Boolean) {
        val historyJson = prefs.getString(QIYAM_HISTORY_KEY, null)
        val currentHistory = if (historyJson != null) {
            runCatching { json.decodeFromString<MutableList<QiyamLogDTO>>(historyJson) }.getOrNull()
                ?: mutableListOf()
        } else {
            mutableListOf()
        }
        // Remove any existing entry with the same date
        currentHistory.removeAll { it.date == date }
        // Add new entry
        currentHistory.add(QiyamLogDTO(date, prayed))
        // Save back
        val newJson = json.encodeToString(currentHistory)
        prefs.edit { putString(QIYAM_HISTORY_KEY, newJson) }
    }

    override fun getQiyamHistory(): List<QiyamLog> {
        val historyJson = prefs.getString(QIYAM_HISTORY_KEY, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<QiyamLogDTO>>(historyJson)
                .map { it.toDomain() }
                .sortedByDescending { it.date }
        }.getOrNull() ?: emptyList()
    }

    override fun getCurrentStreak(): Int {
        val history = getQiyamHistory()
        if (history.isEmpty()) return 0

        // Map history by date for quick lookup
        val historyMap = history.associateBy { it.date }

        val calendar = Calendar.getInstance()
        var streak = 0

        while (true) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)

            val log = historyMap[dateStr]
            if (log == null || log.prayed == false) {
                break
            }
            streak++
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        return streak
    }
}