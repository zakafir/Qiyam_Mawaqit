package com.zakafir.presentation.di

import com.zakafir.presentation.add_edit.AddEditAlarmViewModel
import com.zakafir.presentation.list.AlarmListViewModel
import com.zakafir.presentation.ringtone_list.RingtoneListViewModel
import com.zakafir.data.core.domain.ringtone.NameAndUri
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.lazyModule

val featureAlarmPresentationModule = lazyModule {
    viewModelOf(::AlarmListViewModel)
    viewModel { (alarmId: String?) ->
        AddEditAlarmViewModel(
            alarmId = alarmId,
            alarmRepository = get(),
            validateAlarmUseCase = get(),
            ringtoneManager = get()
        )
    }
    viewModel { (selectedRingtone: NameAndUri) -> RingtoneListViewModel(selectedRingtone, get()) }
}