package com.zakafir.presentation

import kotlinx.serialization.Serializable

@Serializable
data class QiyamAlarm(val hour: String?, val minute: String?, val alarmName: String?)
