package com.zakafir.domain

import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.domain.model.QiyamWindow

interface PrayerTimesRepository {
    suspend fun getPrayersTime(masjidId: String): Result<YearlyPrayers>
    suspend fun computeQiyamWindow(masjidId: String): Result<QiyamWindow>
    suspend fun searchMosques(word: String): List<Pair<String?, String?>>
}
