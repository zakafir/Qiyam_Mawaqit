package com.zakafir.data

import com.zakafir.data.local.LocalDataSourceImpl
import com.zakafir.data.remote.RemoteDataSourceImpl
import com.zakafir.domain.LocalDataSource
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.RemoteDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.lazyModule

object DataModule {
    fun getModule(): Lazy<Module> = lazyModule {
        singleOf(::PrayerTimesApi)
        singleOf(::LocalDataSourceImpl) { bind<LocalDataSource>() }
        singleOf(::RemoteDataSourceImpl) { bind<RemoteDataSource>() }
        singleOf(::PrayerTimesRepositoryImpl) { bind<PrayerTimesRepository>() }
    }
}
