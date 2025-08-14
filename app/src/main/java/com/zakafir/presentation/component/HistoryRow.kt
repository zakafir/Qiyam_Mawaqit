package com.zakafir.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakafir.domain.model.QiyamLog


@Composable
fun HistoryRow(log: QiyamLog) {
    val prayed = when (log.prayed) {
        true -> "Prayed"
        false -> "Missed"
        null -> "Unknown (please modify)"
    }
    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            log.prayed == true -> Color(0xFF10B981) // green
                            log.prayed == false -> Color(0xFFEF4444) // red
                            else -> Color(0xFF9CA3AF) // gray for unknown
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    log.prayed == true -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    log.prayed == false -> Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    else -> Icon(Icons.Default.Create, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(log.date, fontWeight = FontWeight.SemiBold)
                Text(prayed, color = Color.Gray)
            }
        }
    }
}