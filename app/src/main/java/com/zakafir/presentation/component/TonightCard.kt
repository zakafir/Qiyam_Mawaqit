package com.zakafir.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zakafir.domain.model.QiyamMode
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.presentation.QiyamUiState
import com.zakafir.presentation.screen.QiyamModeSelector
import kotlinx.datetime.LocalDateTime

@Composable
fun TonightCard(
    qiyamUiState: QiyamUiState,
    onSchedule: (LocalDateTime) -> Unit,
    onTestAlarmUi: () -> Unit,
    onModeChange: (QiyamMode) -> Unit,
    date: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Tonight",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            QiyamModeSelector(
                mode = qiyamUiState.mode,
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
        onSchedule = {},
        onTestAlarmUi = {},
        date = "2023-01-01",
        onModeChange = {}
    )
}