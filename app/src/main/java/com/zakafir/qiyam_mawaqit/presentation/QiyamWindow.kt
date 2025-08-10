package com.zakafir.qiyam_mawaqit.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zakafir.qiyam_mawaqit.presentation.component.StatChip
import com.zakafir.qiyam_mawaqit.presentation.screen.HomeScreen
import com.zakafir.qiyam_mawaqit.presentation.screen.QiyamApp
import com.zakafir.qiyam_mawaqit.presentation.screen.SettingsScreen
import com.zakafir.qiyam_mawaqit.presentation.screen.WakeScreen
import kotlinx.datetime.*

// ---------- Models (UI-only, platform-agnostic) ----------

data class QiyamWindow(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val suggestedWake: LocalDateTime
)

data class QiyamLog(val date: LocalDate, val woke: Boolean, val prayed: Boolean)

// Simple in-memory demo state for previews / UI wiring
class QiyamUiState(
    val window: QiyamWindow,
    val today: LocalDate,
    val streakDays: Int,
    val weeklyGoal: Int,
    val bufferMinutes: Int,
    val history: List<QiyamLog>
)

// ---------- Navigation ----------

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Wake : Screen("wake")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}


// ---------- Screens ----------


@Composable
fun TonightCard(
    window: QiyamWindow,
    onSchedule: (LocalDateTime) -> Unit,
    onTestAlarmUi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tonight", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("Last third", timeRange(window.start, window.end))
                StatChip("Suggested", timeOnly(window.suggestedWake))
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSchedule(window.suggestedWake) }) { Text("Schedule") }
                OutlinedButton(onClick = onTestAlarmUi) { Text("Test alarm UI") }
            }
        }
    }
}

@Composable
fun HistoryScreen(history: List<QiyamLog>) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(history) { item ->
            HistoryRow(item)
        }
    }
}

@Composable
fun HistoryRow(log: QiyamLog) {
    val prayed = if (log.prayed) "Prayed" else "Missed"
    val woke = if (log.woke) "Woke" else "Slept"
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (log.prayed) Color(0xFF10B981) else Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(log.date.toString(), fontWeight = FontWeight.SemiBold)
                Text("$woke · $prayed", color = Color.Gray)
            }
        }
    }
}

// ---------- Reusable UI ----------

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
    val start = LocalDateTime(year = today.year, monthNumber = today.monthNumber,dayOfMonth =  today.dayOfMonth, hour =0, minute = 4, second = 0)
    val end = LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, 0,5, 10)
    val suggested = LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, 0,4, 40)
    return QiyamUiState(
        window = QiyamWindow(start, end, suggested),
        today = today,
        streakDays = 5,
        weeklyGoal = 3,
        bufferMinutes = 12,
        history = List(10) { i ->
            val d = today.minus(DatePeriod(days = i + 1))
            QiyamLog(d, woke = i % 2 == 0, prayed = i % 3 != 0)
        }
    )
}

// ---------- Previews ----------
