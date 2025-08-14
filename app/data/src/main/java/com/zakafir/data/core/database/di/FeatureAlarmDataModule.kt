package com.zakafir.data.core.database.di

import com.zakafir.data.AlarmRepositoryImpl
import com.zakafir.domain.repository.AlarmRepository
import com.zakafir.domain.usecase.GetFutureDateUseCase
import com.zakafir.domain.usecase.GetTimeLeftInSecondsUseCase
import com.zakafir.domain.usecase.GetTimeToSleepInSecondsUseCase
import com.zakafir.domain.usecase.ValidateAlarmUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import org.koin.dsl.module

val featureAlarmDataModule = lazyModule {
    singleOf(::AlarmRepositoryImpl).bind<AlarmRepository>()
    singleOf(::ValidateAlarmUseCase)
    singleOf(::GetFutureDateUseCase)
    singleOf(::GetTimeLeftInSecondsUseCase)
    singleOf(::GetTimeToSleepInSecondsUseCase)
}