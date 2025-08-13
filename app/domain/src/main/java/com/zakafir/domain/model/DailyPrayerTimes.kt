package com.zakafir.domain.model

data class PrayerTimes(
    val date: String? = null,
    val fajr: String,
    val sunset: String,
    val dohr: String,
    val asr: String,
    val maghreb: String,
    val icha: String
)

data class YearlyPrayers(
    val deducedMasjidId: String,
    val prayerTimes: List<PrayerTimes>
)

data class QiyamWindow(val start: String, val end: String)

sealed class QiyamMode {
    data object LastThird : QiyamMode()
    data object Dawud : QiyamMode()
    data object AfterIsha : QiyamMode()
    data object LastHalf : QiyamMode()

}