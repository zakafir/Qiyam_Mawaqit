package com.zakafir.domain

import com.zakafir.domain.model.Prayers
import com.zakafir.domain.model.QiyamWindow

enum class DataSource { REMOTE, LOCAL, ASSET }

interface PrayerTimesRepository {
    suspend fun getPrayersTime(masjidId: String): Result<Prayers>
    suspend fun computeQiyamWindow(masjidId: String): Result<QiyamWindow>

    suspend fun findAnyLocalMasjidId(): String?

    fun lastDataSource(): DataSource?
}
