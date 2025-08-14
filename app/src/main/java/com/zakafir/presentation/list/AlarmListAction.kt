package com.zakafir.presentation.list

import com.zakafir.domain.model.Alarm
import com.zakafir.domain.model.DayValue

sealed interface AlarmListAction {
    data object OnAddNewAlarmClick: AlarmListAction
    data class OnToggleAlarm(val alarm: Alarm): AlarmListAction
    data class OnToggleDayOfAlarm(val day: DayValue, val alarm: Alarm): AlarmListAction
    data class OnAlarmClick(val id: String): AlarmListAction
    data class OnDeleteAlarmClick(val id: String): AlarmListAction
}