package com.zakafir.scheduler_receiver.di

import com.zakafir.BaseApplication
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.lazyModule

val appModule = lazyModule {
    single<CoroutineScope> {
        (androidApplication() as BaseApplication).applicationScope
    }
}