package com.zakafir.presentation

import com.zakafir.domain.model.MosqueDetails
import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.presentation.screen.NapConfig
import com.zakafir.presentation.WorkingUiState

data class PrayerUiState(
    val masjidId: String = "",
    val enableIshaBuffer: Boolean = false,
    val enablePostFajr: Boolean = false,
    val enableNaps: Boolean = false,
    val yearlyPrayers: YearlyPrayers? = null,
    val qiyamUiState: QiyamUiState? = null,
    val workState: WorkingUiState = WorkingUiState(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val streak: Int = 0,
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
    val searchResults: List<MosqueDetails> = emptyList(),
    val selectedMosque: MosqueDetails? = null
)