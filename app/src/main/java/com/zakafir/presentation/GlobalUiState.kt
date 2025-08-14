package com.zakafir.presentation

import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.NapConfig
import com.zakafir.domain.model.YearlyPrayers

data class GlobalUiState(
    val qiyamUiState: QiyamUiState? = null,
    val settingsUiState: SettingsUiState? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchResults: List<MosqueDetails> = emptyList(),
    val yearlyPrayers: YearlyPrayers? = null,
    val masjidId: String = "",
    val streak: Int = 0,
    val bufferMinutesForWuduaa: Int = 15,
    val selectedMasjid: MosqueDetails? = null
)

data class SettingsUiState(
    val workStart: String = "09:00",
    val workEnd: String = "17:00",
    val commuteToMin: Int = 30,
    val commuteFromMin: Int = 30,
    val enableIshaBuffer: Boolean = false,
    val enablePostFajr: Boolean = false,
    val enableNaps: Boolean = false,
    val allowPostFajr: Boolean = false,
    val naps: List<NapConfig> = emptyList(),
    val bufferWuduaMinutes: Int = 15,
    val desiredSleepHours: Float = 7.5f,
    val postFajrBufferMin: Int = 60,
    val ishaBufferMin: Int = 30,
    val minNightStart: String = "22:00",
    val disallowPostFajrIfFajrAfter: String = "07:30",
    val latestMorningEnd: String = "07:30",
)