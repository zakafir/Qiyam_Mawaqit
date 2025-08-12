package com.zakafir.domain

import com.zakafir.domain.model.YearlyPrayers

interface LocalDataSource {
    suspend fun getLocalPrayersTime(masjidId: String): Result<YearlyPrayers>
    fun readYearlyPrayers(masjidId: String): YearlyPrayers?
    fun writeYearlyPrayers(masjidId: String, yearly: YearlyPrayers)
}
