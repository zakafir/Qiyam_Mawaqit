package com.zakafir.domain

import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.domain.model.QiyamWindow

interface PrayerTimesRepository {
    suspend fun getPrayersTime(masjidId: String): Result<YearlyPrayers>
    suspend fun computeQiyamWindow(todaysPrayerTimes: PrayerTimes?, tommorowsPrayerTimes: PrayerTimes?): Result<QiyamWindow>
    suspend fun searchMosques(word: String): List<Pair<String?, String?>>
    suspend fun getLastSelectedMasjidId(): String?
    suspend fun saveLastSelectedMasjidId(masjidId: String)
}
