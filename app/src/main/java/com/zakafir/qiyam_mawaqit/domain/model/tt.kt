/*
package com.zakafir.qiyam_mawaqit.domain.model


data class PrayerTimes(
    val date: LocalDate,           // civil date D
    val maghrib: LocalDateTime,    // tz-aware
    val fajrNext: LocalDateTime    // fajr of D+1
)

data class QiyamWindow(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val suggestedWake: LocalDateTime
)

fun computeQiyamWindow(times: PrayerTimes, bufferMinutes: Int = 12): QiyamWindow {
    require(times.fajrNext > times.maghrib) { "Invalid night window" }
    val nightDuration = times.fajrNext - times.maghrib  // kotlin.time Duration
    val lastThirdStart = times.fajrNext - (nightDuration / 3)
    val center = lastThirdStart + ((times.fajrNext - lastThirdStart) / 2)
    val suggested = center - bufferMinutes.minutes
    return QiyamWindow(lastThirdStart, times.fajrNext, suggested)
}*/
