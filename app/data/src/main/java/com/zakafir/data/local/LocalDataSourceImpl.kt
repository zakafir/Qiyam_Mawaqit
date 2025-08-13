package com.zakafir.data.local

import android.content.Context
import android.content.SharedPreferences
import com.zakafir.data.mapper.toData
import com.zakafir.data.mapper.toDomain
import com.zakafir.data.model.PrayersDTO
import com.zakafir.domain.LocalDataSource
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.domain.model.YearlyPrayers
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import androidx.core.content.edit
import com.zakafir.domain.model.QiyamMode


class LocalDataSourceImpl(
    private val context: Context,
    private val prefs: SharedPreferences,
): LocalDataSource {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

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

    override fun enableNaps(enabled: Boolean) {
        prefs.edit { putBoolean("enable_naps", enabled) }
    }

    override fun enablePostFajr(enabled: Boolean) {
        prefs.edit { putBoolean("enable_post_fajr", enabled) }
    }

    override fun enableIshaBuffer(enabled: Boolean) {
        prefs.edit { putBoolean("enable_isha_buffer", enabled) }
    }
}