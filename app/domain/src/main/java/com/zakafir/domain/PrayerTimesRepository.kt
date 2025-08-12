package com.zakafir.domain

import com.zakafir.domain.model.Prayers
import com.zakafir.domain.model.QiyamWindow

interface PrayerTimesRepository {
    suspend fun getPrayersTime(): Result<Prayers>
    suspend fun computeQiyamWindow(): QiyamWindow
    suspend fun getY()
}
