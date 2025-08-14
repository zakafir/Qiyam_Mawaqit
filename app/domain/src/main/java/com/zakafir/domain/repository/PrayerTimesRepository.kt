package com.zakafir.domain.repository

import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamLog
import com.zakafir.domain.model.QiyamMode
import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.domain.model.QiyamWindow

interface PrayerTimesRepository {
    suspend fun getPrayersTime(masjidId: String, displayedMasjidName: String): Result<YearlyPrayers>
    suspend fun computeQiyamWindow(mode: QiyamMode, todaysPrayerTimes: PrayerTimes?, tommorowsPrayerTimes: PrayerTimes?): Result<QiyamWindow>
    suspend fun searchMosques(word: String): List<MosqueDetails>
    suspend fun getLastSelectedMasjidId(): String?
    suspend fun saveLastSelectedMasjidId(masjidId: String)
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

    // Naps
    fun setNapsSerialized(serialized: String)

    fun logQiyam(date: String, prayed: Boolean)

    fun getCurrentStreak(): Int
    fun getQiyamStatusForDate(date: String): Boolean
    fun getQiyamHistory(): List<QiyamLog>
}
