package com.zakafir.qiyam_mawaqit.domain

import android.content.Context
import com.zakafir.qiyam_mawaqit.data.model.PrayerTimes
import com.zakafir.qiyam_mawaqit.data.model.Prayers
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Locale

data class QiyamWindowDTO(val start: String, val end: String)

interface PrayerTimesRepository {
    /** Returns only today's prayers wrapped into Prayers(Result). */
    suspend fun getPrayersTime(context: Context): Result<Prayers>
    /** Computes last third of *tonight* using today's Isha and tomorrow's Fajr. */
    fun computeQiyamWindow(context: Context): QiyamWindowDTO
}

class PrayerTimesRepositoryImpl() : PrayerTimesRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val monthCache = mutableMapOf<Int, List<PrayerTimes>>()

    override suspend fun getPrayersTime(context: Context): Result<Prayers> = runCatching {
        val todayCal = Calendar.getInstance()
        val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val today = getPrayerTimesFor(context, todayCal)
        val tomorrow = getPrayerTimesFor(context, tomorrowCal)

        Prayers(listOf(today, tomorrow))
    }

    override fun computeQiyamWindow(context: Context): QiyamWindowDTO {
        // 1) Load today and tomorrow prayers (tonight's window)
        val todayCal = Calendar.getInstance()
        val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val today = getPrayerTimesFor(context, todayCal)
        val tomorrow = getPrayerTimesFor(context, tomorrowCal)

        val startCal = (todayCal.clone() as Calendar).apply {
            val (hIsha, mIsha) = parseHourMinute(today.icha)
            set(Calendar.HOUR_OF_DAY, hIsha)
            set(Calendar.MINUTE, mIsha)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = (tomorrowCal.clone() as Calendar).apply {
            val (hFajr, mFajr) = parseHourMinute(tomorrow.fajr)
            set(Calendar.HOUR_OF_DAY, hFajr)
            set(Calendar.MINUTE, mFajr)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        // Guard: if end <= start (data issue), roll end to next day
        val fixedEnd = if (end <= start) endCal.apply { add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis else end

        val duration = fixedEnd - start
        val lastThirdStart = start + (duration * 2) / 3

        val lastThirdCal = (todayCal.clone() as Calendar).apply { timeInMillis = lastThirdStart }
        val endCalFixed = (tomorrowCal.clone() as Calendar).apply { timeInMillis = fixedEnd }

        return QiyamWindowDTO(
            start = formatHHmm(lastThirdCal),
            end = formatHHmm(endCalFixed)
        )
    }

    /** Gets prayer times for the given date, using a per-month in-memory cache. */
    private fun getPrayerTimesFor(context: Context, calendar: Calendar): PrayerTimes {
        val monthValue = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val monthItems = monthCache[monthValue] ?: run {
            val monthFile = "$monthValue.json"
            val jsonString = context.assets
                .open(monthFile)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val parsed = json.decodeFromString<List<PrayerTimes>>(jsonString)
            monthCache[monthValue] = parsed
            parsed
        }
        val index = (dayOfMonth - 1).coerceIn(0, monthItems.lastIndex)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dateStr = String.format(Locale.FRANCE, "%04d-%02d-%02d", year, month, day)
        val item = monthItems[index]
        return item.copy(date = dateStr)
    }

    private fun parseHourMinute(hhmm: String): Pair<Int, Int> {
        val parts = hhmm.trim().split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h to m
    }

    private fun formatHHmm(cal: Calendar): String {
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return pad2(h) + ":" + pad2(m)
    }

    private fun pad2(n: Int): String = if (n < 10) "0$n" else n.toString()
}
