package com.zakafir.presentation

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.lazyModule

object PresentationModule {
    fun getModule(): Lazy<Module> = lazyModule {
        viewModelOf(::PrayerTimesViewModel)
    }
}
