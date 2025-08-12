package com.zakafir.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PrayerTimesDTO(
    val date: String? = null,
    val fajr: String,
    val sunset: String,
    val dohr: String,
    val asr: String,
    val maghreb: String,
    val icha: String
)

@Serializable
data class PrayersDTO(
    val deducedMasjidId: String,
    val prayerTimesDTO: List<PrayerTimesDTO>
)

@Serializable
data class QiyamWindowDTO(val start: String, val end: String)