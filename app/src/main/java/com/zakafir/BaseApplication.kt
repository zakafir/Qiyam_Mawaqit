package com.zakafir

import android.app.Application
import com.zakafir.data.DataModule
import com.zakafir.presentation.PresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.waitKoinStart
import org.koin.core.lazyModules

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val modules = listOf(
            DataModule.getModule(),
            PresentationModule.getModule(),
            //DomainModule.getModule(),
        )
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            lazyModules(modules)
        }
        waitKoinStart()
    }
}