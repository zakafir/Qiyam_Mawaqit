package com.zakafir.presentation

import androidx.compose.runtime.*
import kotlinx.datetime.*

@Composable
fun timeOnly(dt: LocalDateTime): String {
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    return "$h:$m"
}
