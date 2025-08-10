package com.zakafir.qiyam_mawaqit.domain

import com.zakafir.qiyam_mawaqit.data.PrayerTimesClient
import com.zakafir.qiyam_mawaqit.data.model.PrayerTimes

interface PrayerTimesRepository {
    suspend fun getAssalamArgenteuil(): Result<PrayerTimes>
}

class PrayerTimesRepositoryImpl(
    private val api: PrayerTimesClient
) : PrayerTimesRepository {
    private var cache: PrayerTimes? = null

    override suspend fun getAssalamArgenteuil(): Result<PrayerTimes> = runCatching {
        // optimistic: return cache if present while refreshing in background (optional)
        val fresh = api.getPrayerTimes()
        cache = fresh
        fresh
    }.recover { e ->
        cache?.let { return Result.success(it) }
        throw e
    }
}
