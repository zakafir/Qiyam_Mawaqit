package com.zakafir.presentation

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

@Composable
fun timeOnly(dt: LocalDateTime): String {
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    return "$h:$m"
}
