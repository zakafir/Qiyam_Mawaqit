package com.zakafir.presentation

import kotlinx.datetime.LocalDateTime

data class QiyamUiState(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val suggestedWake: LocalDateTime
)