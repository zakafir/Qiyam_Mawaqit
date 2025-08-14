package com.zakafir.data.core.database.di

import androidx.room.Room
import com.zakafir.data.core.database.AlarmDatabase
import com.zakafir.data.core.database.alarm.AlarmDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.lazyModule
import org.koin.dsl.module

val coreDatabaseModule = lazyModule {
    single<AlarmDatabase> {
        Room.databaseBuilder(
            context = androidApplication(),
            klass = AlarmDatabase::class.java,
            name = "alarms.db"
        ).build()
    }
    single<AlarmDao> {
        get<AlarmDatabase>().alarmDao
    }
}