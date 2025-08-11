package com.zakafir.qiyam_mawaqit.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakafir.qiyam_mawaqit.presentation.QiyamWindow
import com.zakafir.qiyam_mawaqit.presentation.timeOnly
import com.zakafir.qiyam_mawaqit.presentation.timeRange
import kotlinx.datetime.LocalDateTime

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