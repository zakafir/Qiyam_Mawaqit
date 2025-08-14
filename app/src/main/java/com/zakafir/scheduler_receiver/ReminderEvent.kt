package com.zakafir.scheduler_receiver

import com.zakafir.domain.model.Alarm

sealed interface ReminderEvent {
    data class OnAlarmFetched(val alarm: Alarm): ReminderEvent
    data object OnTimerExpired: ReminderEvent
    data object AlarmIsNotExisting: ReminderEvent
}