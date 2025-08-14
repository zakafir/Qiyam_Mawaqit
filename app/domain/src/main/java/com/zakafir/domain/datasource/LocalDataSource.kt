package com.zakafir.domain.datasource

import com.zakafir.domain.model.NapConfig
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamLog
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
    fun updateWorkEnd(it: String)
    fun updateWorkStart(it: String)
    fun updateCommuteToMin(it: Int)
    fun updateCommuteFromMin(it: Int)

    // Sleep planner settings persistence
    fun setDesiredSleepMinutes(minutes: Int)
    fun setPostFajrBufferMin(minutes: Int)
    fun setIshaBufferMin(minutes: Int)
    fun setMinNightStart(hhmm: String)
    fun setDisallowPostFajrIfFajrAfter(hhmm: String)
    fun setLatestMorningEnd(hhmm: String)

    // Work & commute
    fun setCommuteFromMin(minutes: Int)

    // Naps: simple serialization, e.g. "12:00|30;18:00|20"
    fun setNapsSerialized(serialized: String)

    fun logQiyam(date: String, prayed: Boolean)

    fun getCurrentStreak(): Int

    fun getQiyamHistory(): List<QiyamLog>

    fun saveLastSelectedMasjidName(masjidId: String)

    fun updatePostFajrBuffer(v: Int)
    fun updateIshaBuffer(v: Int)
    fun updateMinNightStart(v: String)
    fun updatePostFajrCutoff(v: String)

    fun updateNap(index: Int, config: NapConfig)
    fun addNap()

    fun updateDesiredSleepHours(v: Float)
    fun updateBufferMinutes(v: Int)
    fun updateAllowPostFajr (allow: Boolean)
    fun updateLatestMorningEnd(v: String)
    fun removeNap(index: Int)
}
