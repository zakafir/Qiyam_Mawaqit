package com.zakafir.qiyam_mawaqit.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

// ---------- Models (UI-only, platform-agnostic) ----------

data class QiyamWindow(
    val start: LocalDateTime, val end: LocalDateTime, val suggestedWake: LocalDateTime
)

@Serializable
data class QiyamLog(
    val date: LocalDate,
    val woke: Boolean,
    val prayed: Boolean
)
@Composable
fun StreakHeader(streak: Int, weeklyGoal: Int) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text("Current streak", style = MaterialTheme.typography.titleMedium)
                Text("$streak days", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            AssistChip(onClick = {}, label = { Text("Goal: $weeklyGoal/wk") })
        }
    }
}


// ---------- Time helpers (UI formatting) ----------

@Composable
fun timeRange(start: LocalDateTime, end: LocalDateTime): String =
    "${timeOnly(start)} – ${timeOnly(end)}"

@Composable
fun timeOnly(dt: LocalDateTime): String {
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    return "$h:$m"
}

// ---------- Preview data ----------

fun demoState(): QiyamUiState {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val start = LocalDateTime(
        year = today.year,
        monthNumber = today.monthNumber,
        dayOfMonth = today.dayOfMonth,
        hour = 0,
        minute = 4,
        second = 0
    )
    val end = LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, 0, 5, 10)
    val suggested = LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, 0, 4, 40)
    return QiyamUiState(
        start = start,
        end = end,
        suggestedWake = suggested
    )
}
