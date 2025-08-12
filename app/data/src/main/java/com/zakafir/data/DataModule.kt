package com.zakafir.data

import com.zakafir.domain.PrayerTimesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.lazyModule

object DataModule {
    fun getModule(): Lazy<Module> = lazyModule {
        singleOf(::PrayerTimesApi)
        single {
            PrayerTimesRepositoryImpl(
                context = androidContext(),
                api = get()
            )
        } bind PrayerTimesRepository::class
    }
}
