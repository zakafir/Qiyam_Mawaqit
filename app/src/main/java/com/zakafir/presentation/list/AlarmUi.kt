package com.zakafir.presentation.list

import com.zakafir.domain.model.Alarm

data class AlarmUi(
    val alarm: Alarm,
    val timeLeftInSeconds: Long,
)