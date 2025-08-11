package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakafir.qiyam_mawaqit.presentation.demoState
import com.zakafir.qiyam_mawaqit.presentation.timeOnly
import kotlinx.datetime.LocalDateTime

@Composable
fun WakeScreen(
    time: LocalDateTime,
    onImUp: () -> Unit,
    onMarkPrayed: () -> Unit,
    onSnooze: () -> Unit
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("Time for Qiyam", color = Color(0xFFE5E7EB), fontSize = 20.sp)
            Text(timeOnly(time), color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onImUp) { Text("I'm up") }
                OutlinedButton(onClick = onSnooze) { Text("Snooze 5") }
            }
            AssistChip(
                onClick = onMarkPrayed,
                label = { Text("Mark prayed") },
                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewWake() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        WakeScreen(
            time = LocalDateTime(2025, 1, 1, 3, 30),
            onImUp = {},
            onMarkPrayed = {},
            onSnooze = {}
        )
    }
}
