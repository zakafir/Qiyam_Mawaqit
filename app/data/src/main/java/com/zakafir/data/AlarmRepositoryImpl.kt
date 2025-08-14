package com.zakafir.data

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.zakafir.data.core.domain.ringtone.RingtoneManager
import com.zakafir.data.core.util.hideNotification
import com.zakafir.data.core.util.isOreoPlus
import com.zakafir.domain.model.Alarm
import com.zakafir.domain.model.AlarmConstants
import com.zakafir.domain.repository.AlarmRepository
import com.zakafir.domain.AlarmScheduler
import com.zakafir.data.core.database.alarm.AlarmDao
import com.zakafir.data.mapper.toAlarm
import com.zakafir.data.mapper.toAlarmEntity
import com.zakafir.domain.model.DayValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler,
    private val ringtoneManager: RingtoneManager,
    private val context: Context
): AlarmRepository {

    private val vibrator = context.getSystemService(Vibrator::class.java)

    override fun getAll(): Flow<List<Alarm>> {
        return alarmDao.getAll().map { alarms ->
            alarms.map { it.toAlarm() }
        }
    }

    override suspend fun getById(id: String): Alarm? {
        return alarmDao.getById(id)?.toAlarm()
    }

    override suspend fun upsert(alarm: Alarm) {
        alarmDao.upsert(alarm.toAlarmEntity())
        alarmScheduler.schedule(alarm)
    }

    override suspend fun toggle(alarm: Alarm) {
        val isEnabled = !alarm.enabled
        alarmDao.upsert(alarm.copy(enabled = isEnabled).toAlarmEntity())

        if (isEnabled) {
            alarmScheduler.schedule(alarm)
        } else {
            alarmScheduler.cancel(alarm)
        }
    }

    override suspend fun toggleDay(day: DayValue, alarm: Alarm) {
        // Cancel alarm first to avoid conflict.
        alarmScheduler.cancel(alarm)


        val repeatDays = alarm.repeatDays.toMutableSet()

        // Remove it from the set if it exists.
        if (repeatDays.contains(day)) {
            repeatDays.remove(day)
        } else { // Or else, add it in the set
            repeatDays.add(day)
        }

        val updatedAlarm = alarm.copy(repeatDays = repeatDays)
        upsert(updatedAlarm)
    }

    override suspend fun disableAlarmById(id: String) {
        alarmDao.disableAlarmById(id)
    }

    override suspend fun deleteById(id: String) {
        getById(id)?.let {
            alarmScheduler.cancel(it)
        }

        alarmDao.deleteById(id)
    }

    override suspend fun scheduleAllEnabledAlarms() {
        getAll().first().map { alarm ->
            if (alarm.enabled) {
                alarmScheduler.schedule(alarm)
            }
        }
    }

    override fun setupEffects(alarm: Alarm) {
        val pattern = AlarmConstants.VIBRATE_PATTERN_LONG_ARR
        if (isOreoPlus() && alarm.vibrate) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }

        val volume = (alarm.volume / 100f)
        if (!ringtoneManager.isPlaying()) {
            ringtoneManager.play(uri = alarm.ringtoneUri, isLooping = true, volume = volume)
        }
    }

    override fun stopEffectsAndHideNotification(alarm: Alarm) {
        context.hideNotification(alarm.id.hashCode())
        ringtoneManager.stop()
        vibrator.cancel()
    }

    override fun snoozeAlarm(alarm: Alarm) {
        alarmScheduler.schedule(
            alarm = alarm,
            shouldSnooze = true
        )
    }
}