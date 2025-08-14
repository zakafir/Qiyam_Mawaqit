package com.zakafir.presentation.util

import com.zakafir.domain.model.Alarm

fun getDummyAlarm(
    name: String,
    hour: Int,
    minute: Int,
    enabled: Boolean
) = Alarm(
    name = name,
    hour = hour,
    minute = minute,
    enabled = enabled,
    repeatDays = setOf(),
    volume = 70,
    ringtoneUri = "",
    vibrate = true
)