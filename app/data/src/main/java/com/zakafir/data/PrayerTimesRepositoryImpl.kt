package com.zakafir.data

import com.zakafir.domain.LocalDataSource
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.RemoteDataSource
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.domain.model.YearlyPrayers

class PrayerTimesRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : PrayerTimesRepository {

    override suspend fun getPrayersTime(masjidId: String): Result<YearlyPrayers> {
        // 1) Try local first
        localDataSource.readYearlyPrayers(masjidId)?.let { return Result.success(it) }

        // 2) Fetch remote and persist locally
        val remote = remoteDataSource.getYearlyPrayersTime(masjidId)
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

    override suspend fun computeQiyamWindow(masjidId: String): Result<QiyamWindow> {
        TODO("Not yet implemented")
    }

    override suspend fun searchMosques(word: String): List<Pair<String?, String?>> {
        return remoteDataSource.searchMosques(word)
    }
}