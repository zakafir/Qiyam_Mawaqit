package com.zakafir.data.remote

import com.zakafir.data.PrayerTimesApi
import com.zakafir.data.PrayerTimesApi.YearCalendarDTO
import com.zakafir.data.mapper.toDomain
import com.zakafir.data.model.PrayerTimesDTO
import com.zakafir.data.model.PrayersDTO
import com.zakafir.domain.RemoteDataSource
import com.zakafir.domain.model.YearlyPrayers
import java.util.Calendar
import java.util.Locale

class RemoteDataSourceImpl(
    private val api: PrayerTimesApi
) : RemoteDataSource {

    override suspend fun getYearlyPrayersTime(masjidId: String): Result<YearlyPrayers> = runCatching {
        val calendarDto = api.getYearlyCalendar(masjidId)
        val list = mapYearCalendar(calendarDto)
        PrayersDTO(deducedMasjidId = masjidId, prayerTimesDTO = list).toDomain()
    }

    override suspend fun searchMosques(word: String): List<Pair<String?, String?>> {
        return api.searchMosques(word).map { it.displayLine to it.slug }
    }

    private fun mapYearCalendar(src: YearCalendarDTO): List<PrayerTimesDTO> {
        val out = mutableListOf<PrayerTimesDTO>()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        src.calendar.forEachIndexed { monthIdx, daysMap ->
            daysMap
                .toList()
                .sortedBy { it.first.toIntOrNull() ?: Int.MAX_VALUE }
                .forEach { (dayKey, times) ->
                    val day = dayKey.toIntOrNull() ?: return@forEach
                    out += PrayerTimesDTO(
                        fajr = times.getOrNull(0) ?: "00:00",
                        sunset = times.getOrNull(1)
                            ?: "00:00", // API may provide sunrise here; model uses 'sunset'
                        dohr = times.getOrNull(2) ?: "00:00",
                        asr = times.getOrNull(3) ?: "00:00",
                        maghreb = times.getOrNull(4) ?: "00:00",
                        icha = times.getOrNull(5) ?: "00:00",
                        date = "%04d-%02d-%02d".format(Locale.FRANCE, year, monthIdx + 1, day)
                    )
                }
        }
        return out
    }
}