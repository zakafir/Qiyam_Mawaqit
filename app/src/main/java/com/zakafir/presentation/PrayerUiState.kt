package com.zakafir.presentation

import com.zakafir.domain.model.QiyamWindow
import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.presentation.screen.NapConfig

data class PrayerUiState(
    val masjidId: String = "",
    val yearlyPrayers: YearlyPrayers? = null,
    val qiyamWindow: QiyamWindow? = null,
    val qiyamUiState: QiyamUiState? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val streak: Int = 11,
    val weeklyGoal: Int = 3,
    // Advanced Sleep Planner controls (values only)
    val desiredSleepHours: Float = 7.5f,
    val postFajrBufferMin: Int = 60,
    val ishaBufferMin: Int = 30,
    val minNightStart: String = "21:30",
    val disallowPostFajrIfFajrAfter: String = "06:30",
    val naps: List<NapConfig> = emptyList(),
    val bufferMinutes: Int = 12,
    val allowPostFajr: Boolean = true,
    val latestMorningEnd: String = "07:30",
    val dataSourceLabel: String? = null,
    val searchResults: List<Pair<String?, String?>> = emptyList(),
)