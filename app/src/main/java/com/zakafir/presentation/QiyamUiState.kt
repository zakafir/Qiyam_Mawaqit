package com.zakafir.presentation

import com.zakafir.domain.model.QiyamMode
import com.zakafir.domain.model.QiyamWindow
import kotlinx.datetime.LocalDateTime

data class QiyamUiState(
    val start: String,
    val end: String,
    val duration: String,
    val mode: QiyamMode,
    val window: QiyamWindow? = null,
    val suggestedWake: LocalDateTime
)