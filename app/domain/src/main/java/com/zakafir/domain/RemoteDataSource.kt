package com.zakafir.domain

import com.zakafir.domain.model.YearlyPrayers

interface RemoteDataSource {
    suspend fun getYearlyPrayersTime(masjidId: String): Result<YearlyPrayers>
    suspend fun searchMosques(word: String): List<Pair<String?, String?>>
}
