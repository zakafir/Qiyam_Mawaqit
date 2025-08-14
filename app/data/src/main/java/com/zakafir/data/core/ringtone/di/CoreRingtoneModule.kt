package com.zakafir.data.core.ringtone.di

import com.zakafir.data.core.domain.ringtone.RingtoneManager
import com.zakafir.data.core.ringtone.AndroidRingtoneManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import org.koin.dsl.module

val coreRingtoneModule = lazyModule {
    single { AndroidRingtoneManager(androidContext()) }.bind<RingtoneManager>()
}