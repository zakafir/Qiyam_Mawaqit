package com.zakafir.domain

import com.zakafir.domain.model.Prayers
import com.zakafir.domain.model.QiyamWindow

interface PrayerTimesRepository {
    suspend fun getPrayersTime(masjidId: String): Result<Prayers>
    suspend fun computeQiyamWindow(masjidId: String): QiyamWindow
}
