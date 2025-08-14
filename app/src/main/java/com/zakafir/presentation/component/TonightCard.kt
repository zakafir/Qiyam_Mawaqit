package com.zakafir.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zakafir.domain.model.QiyamMode
import com.zakafir.presentation.QiyamUiState
import kotlinx.datetime.LocalDateTime

@Composable
fun TonightCard(
    qiyamUiState: QiyamUiState,
    onTestAlarmUi: () -> Unit,
    onModeChange: (QiyamMode) -> Unit,
    onLogPrayed: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Tonight",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            SingleChoiceSegmentedButton(
                modes = listOf(
                    QiyamMode.LastThird,
                    QiyamMode.Dawud,
                    QiyamMode.AfterIsha,
                    QiyamMode.LastHalf
                ),
                selectedMode = qiyamUiState.mode,
                onSelect = {
                    onModeChange(it)
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("Qiyam start", qiyamUiState.start)
                StatChip("Qiyam end", qiyamUiState.end)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Duration: ${qiyamUiState.duration}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onTestAlarmUi) { Text("Set alarm to start the Qiyam") }
            }
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    onLogPrayed(true)
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (qiyamUiState.prayed) Color(0xFF10B981) else MaterialTheme.colorScheme.surface,
                    contentColor = if (qiyamUiState.prayed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Log Qiyam as Prayed") }
            println("prayed: ${qiyamUiState.prayed}")
            OutlinedButton(
                onClick = {
                    onLogPrayed(false)
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (!qiyamUiState.prayed) Color.Red else MaterialTheme.colorScheme.surface,
                    contentColor = if (!qiyamUiState.prayed) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Log Qiyam as Missed") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val helperBody = when (qiyamUiState.mode) {
            is QiyamMode.AfterIsha -> "From after Isha until Fajr — any time in this window counts as Qiyam."
            is QiyamMode.LastHalf -> "The last half of the night between Maghrib and Fajr. Preferred if you can wake up in this period."
            is QiyamMode.LastThird -> "The final third of the night between Maghrib and Fajr. Your wake time sits near the center, minus a small buffer for wuḍūʾ."
            is QiyamMode.Dawud -> "Following the prayer of Dawud: sleep half the night, pray one third, then sleep one sixth. This covers the 4th and 5th sixths of the night."
        }
        Column(Modifier.padding(14.dp)) {
            Text("What is the ${qiyamUiState.mode.text} mode?", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(helperBody, color = Color.Gray)
        }
    }
}


@Composable
fun QiyamModeSelector(
    modes: List<QiyamMode>,
    selectedMode: QiyamMode,
    onSelect: (QiyamMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { mode ->
            ModeChip(
                text = mode.text,
                selected = mode == selectedMode,
                onClick = { onSelect(mode) }
            )
        }
    }
}

@Composable
fun SingleChoiceSegmentedButton(
    modes: List<QiyamMode>,
    selectedMode: QiyamMode,
    onSelect: (QiyamMode) -> Unit,
) {

    SingleChoiceSegmentedButtonRow {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = modes.size
                ),
                onClick = { onSelect(mode) },
                selected = mode == selectedMode,
                label = { Text(text = mode.text) }
            )
        }
    }
}

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            tonalElevation = 0.dp,
            modifier = Modifier,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            border = null,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier = Modifier,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Preview
@Composable
fun TonightCardPreview() {
    TonightCard(
        qiyamUiState = QiyamUiState(
            start = "00:00",
            end = "00:00",
            duration = "00:00",
            mode = QiyamMode.LastThird,
            suggestedWake = LocalDateTime(2023, 1, 1, 0, 0)
        ),
        onLogPrayed = {},
        onTestAlarmUi = {},
        onModeChange = {}
    )
}