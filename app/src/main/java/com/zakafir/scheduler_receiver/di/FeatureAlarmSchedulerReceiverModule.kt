package com.zakafir.scheduler_receiver.di

import com.zakafir.domain.AlarmScheduler
import com.zakafir.scheduler_receiver.AndroidAlarmScheduler
import com.zakafir.scheduler_receiver.ReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import org.koin.dsl.module

val featureAlarmSchedulerReceiverModule = lazyModule {
    viewModel { (alarmId: String) -> ReminderViewModel(alarmId, get()) }
    single<AndroidAlarmScheduler> {
        AndroidAlarmScheduler(androidContext(), get())
    }.bind<AlarmScheduler>()
}