package com.zakafir.data

import com.zakafir.data.model.NapConfigDTO
import com.zakafir.domain.datasource.LocalDataSource
import com.zakafir.domain.repository.PrayerTimesRepository
import com.zakafir.domain.datasource.RemoteDataSource
import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.NapConfig
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamLog
import com.zakafir.domain.model.QiyamMode
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.domain.model.YearlyPrayers

class PrayerTimesRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : PrayerTimesRepository {

    override suspend fun getPrayersTime(
        masjidId: String,
        displayedMasjidName: String
    ): Result<YearlyPrayers> {
        // 1) Try local first
        localDataSource.readYearlyPrayers(masjidId)?.let { return Result.success(it) }

        // 2) Fetch remote and persist locally
        val remote = remoteDataSource.getYearlyPrayersTime(masjidId, displayedMasjidName)
        if (remote.isFailure) {
            return Result.failure(
                remote.exceptionOrNull() ?: Exception("Unknown error fetching prayers time")
            )
        }
        remote.onSuccess { yearly ->
            runCatching { localDataSource.writeYearlyPrayers(masjidId, yearly) }
        }
        return remote
    }

    override suspend fun computeQiyamWindow(
        mode: QiyamMode,
        todaysPrayerTimes: PrayerTimes?,
        tommorowsPrayerTimes: PrayerTimes?
    ): Result<QiyamWindow> {
        return localDataSource.computeQiyamWindow(
            mode = mode,
            todaysPrayerTimes,
            tommorowsPrayerTimes
        )
    }

    override suspend fun searchMosques(word: String): List<MosqueDetails> {
        return remoteDataSource.searchMosques(word)
    }

    override suspend fun getLastSelectedMasjidId(): String? {
        return localDataSource.getLastSelectedMasjidId()
    }

    override suspend fun saveLastSelectedMasjidId(masjidId: String) {
        localDataSource.saveLastSelectedMasjidId(masjidId)
    }

    override fun enableNaps(enabled: Boolean) {
        localDataSource.enableNaps(enabled)
    }

    override fun enablePostFajr(enabled: Boolean) {
        localDataSource.enablePostFajr(enabled)
    }

    override fun enableIshaBuffer(enabled: Boolean) {
        localDataSource.enableIshaBuffer(enabled)
    }

    override fun updateWorkEnd(it: String) {
        localDataSource.updateWorkEnd(it)
    }

    override fun updateWorkStart(it: String) {
        localDataSource.updateWorkStart(it)
    }

    override fun updateCommuteToMin(it: Int) {
        localDataSource.updateCommuteToMin(it)
    }

    override fun updateCommuteFromMin(it: Int) {
        localDataSource.updateCommuteFromMin(it)
    }

    override fun setDesiredSleepMinutes(minutes: Int) =
        localDataSource.setDesiredSleepMinutes(minutes)

    override fun setPostFajrBufferMin(minutes: Int) = localDataSource.setPostFajrBufferMin(minutes)
    override fun setIshaBufferMin(minutes: Int) = localDataSource.setIshaBufferMin(minutes)
    override fun setMinNightStart(hhmm: String) = localDataSource.setMinNightStart(hhmm)
    override fun setDisallowPostFajrIfFajrAfter(hhmm: String) =
        localDataSource.setDisallowPostFajrIfFajrAfter(hhmm)

    override fun setLatestMorningEnd(hhmm: String) = localDataSource.setLatestMorningEnd(hhmm)

    override fun setCommuteFromMin(minutes: Int) = localDataSource.setCommuteFromMin(minutes)

    override fun setNapsSerialized(serialized: String) =
        localDataSource.setNapsSerialized(serialized)

    override fun logQiyam(date: String, prayed: Boolean) {
        localDataSource.logQiyam(date, prayed)
    }

    override fun getCurrentStreak(): Int {
        return localDataSource.getCurrentStreak()
    }

    override fun getQiyamStatusForDate(date: String): Boolean {
        return localDataSource.getQiyamHistory().find { it.date == date }?.prayed ?: false
    }

    override fun getQiyamHistory(): List<QiyamLog> {
        return localDataSource.getQiyamHistory()
    }

    override fun updatePostFajrBuffer(v: Int) {
        return localDataSource.updatePostFajrBuffer(v)
    }

    override fun updateIshaBuffer(v: Int) {
        localDataSource.updateIshaBuffer(v)
    }

    override fun updateMinNightStart(v: String) {
        localDataSource.updateMinNightStart(v)
    }

    override fun updatePostFajrCutoff(v: String) {
        localDataSource.updatePostFajrCutoff(v)
    }

    override fun updateNap(index: Int, config: NapConfig) {
        localDataSource.updateNap(index, config)
    }

    override fun addNap() {
        localDataSource.addNap()
    }

    override fun updateDesiredSleepHours(v: Float) {
        localDataSource.updateDesiredSleepHours(v)
    }

    override fun updateBufferMinutes(v: Int) {
        localDataSource.updateBufferMinutes(v)
    }

    override fun updateAllowPostFajr(allow: Boolean) {
        localDataSource.updateAllowPostFajr(allow)
    }

    override fun updateLatestMorningEnd(v: String) {
        localDataSource.updateLatestMorningEnd(v)
    }

    override fun removeNap(index: Int) {
        localDataSource.removeNap(index)
    }

    override fun enablePostMaghrib(enablePostMaghrib: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updatePostMaghribBuffer(postMaghribBufferMin: Int) {
        TODO("Not yet implemented")
    }

}