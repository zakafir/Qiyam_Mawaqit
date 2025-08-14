package com.zakafir.domain.datasource

import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.YearlyPrayers

interface RemoteDataSource {
    suspend fun getYearlyPrayersTime(masjidId: String, displayedMasjidName: String): Result<YearlyPrayers>
    suspend fun searchMosques(word: String): List<MosqueDetails>
}
