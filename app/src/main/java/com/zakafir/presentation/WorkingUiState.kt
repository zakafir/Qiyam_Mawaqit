package com.zakafir.presentation

data class WorkingUiState(
    val workStart: String = "09:00",
    val workEnd: String = "17:00",
    val commuteToMin: Int = 30,
    val commuteFromMin: Int = 30,
)