package com.zakafir.domain

import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamMode
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.domain.model.YearlyPrayers

interface LocalDataSource {
    fun readYearlyPrayers(masjidId: String): YearlyPrayers?
    fun writeYearlyPrayers(masjidId: String, yearly: YearlyPrayers)
    fun computeQiyamWindow(
        mode: QiyamMode,
        todaysPrayerTimes: PrayerTimes?,
        tommorowsPrayerTimes: PrayerTimes?
    ): Result<QiyamWindow>

    fun getLastSelectedMasjidId(): String?
    fun saveLastSelectedMasjidId(masjidId: String)
    fun enableNaps(enabled: Boolean)
    fun enablePostFajr(enabled: Boolean)
    fun enableIshaBuffer(enabled: Boolean)
}
