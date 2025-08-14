package com.zakafir.domain

import com.zakafir.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm, shouldSnooze: Boolean = false)
    fun cancel(alarm: Alarm)
}