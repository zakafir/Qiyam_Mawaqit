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
    val displayedMasjidName: String,
    val prayerTimes: List<PrayerTimes>
)

data class QiyamWindow(val start: String, val end: String)

data class QiyamLog(val date: String, val prayed: Boolean?)

sealed class QiyamMode(val text: String) {
    data object LastThird : QiyamMode("Last third")
    data object Dawud : QiyamMode("Dawud")
    data object AfterIsha : QiyamMode("After Isha")
    data object LastHalf : QiyamMode("Last half")
}