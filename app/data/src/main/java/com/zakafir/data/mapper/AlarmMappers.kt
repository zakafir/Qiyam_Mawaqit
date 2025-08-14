package com.zakafir.data.mapper

import com.zakafir.data.core.database.alarm.AlarmEntity
import com.zakafir.domain.model.Alarm
import com.zakafir.domain.model.DayValue

fun Alarm.toAlarmEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        name = name,
        hour = hour,
        minute = minute,
        enabled = enabled,
        repeatDays = repeatDays.toSetOfInt(),
        volume = volume,
        ringtoneUri = ringtoneUri,
        vibrate = vibrate
    )
}

fun AlarmEntity.toAlarm(): Alarm {
    return Alarm(
        id = id,
        name = name,
        hour = hour,
        minute = minute,
        enabled = enabled,
        repeatDays = repeatDays.toDayValues(),
        volume = volume,
        ringtoneUri = ringtoneUri,
        vibrate = vibrate
    )
}

fun Set<Int>.toDayValues(): Set<DayValue> {
    return this.map { DayValue.entries[it] }.toSet()
}

fun Set<DayValue>.toSetOfInt(): Set<Int> {
    return this.map { it.ordinal }.toSet()
}