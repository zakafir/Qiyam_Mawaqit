package com.zakafir.domain.repository

import com.zakafir.domain.model.Alarm
import com.zakafir.domain.model.DayValue
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAll(): Flow<List<Alarm>>
    suspend fun getById(id: String): Alarm?
    suspend fun upsert(alarm: Alarm)

    /**
     * We can enable or disable an alarm. It should be scheduled or cancelled from AlarmManager
     */
    suspend fun toggle(alarm: Alarm)
    suspend fun toggleDay(day: DayValue, alarm: Alarm)

    /**
     * Disable the alarm after it has been turned off by the user. No need to cancel it from AlarmManager
     */
    suspend fun disableAlarmById(id: String)
    suspend fun deleteById(id: String)
    suspend fun scheduleAllEnabledAlarms()

    /**
     * Play vibration and ringtone
     */
    fun setupEffects(alarm: Alarm)

    /**
     * Stop vibration and ringtone
     */
    fun stopEffectsAndHideNotification(alarm: Alarm)

    fun snoozeAlarm(alarm: Alarm)
}