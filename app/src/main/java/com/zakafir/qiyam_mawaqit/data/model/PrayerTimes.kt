package com.zakafir.qiyam_mawaqit.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimes(
    val fajr: String,
    val sunset: String,
    val dohr: String,
    val asr: String,
    val maghreb: String,
    val icha: String
)

@Serializable
data class Prayers(
    val prayerTimes: List<PrayerTimes>
)