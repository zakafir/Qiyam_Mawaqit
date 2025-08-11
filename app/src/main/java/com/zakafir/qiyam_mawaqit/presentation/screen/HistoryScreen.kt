package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zakafir.qiyam_mawaqit.presentation.QiyamLog
import com.zakafir.qiyam_mawaqit.presentation.component.HistoryRow
import kotlinx.datetime.LocalDate

@Composable
fun HistoryScreen(history: List<QiyamLog>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
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

@Preview(showBackground = true)
@Composable
private fun PreviewHistory() {
    HistoryScreen(
        history = listOf(
            QiyamLog(date = LocalDate(2025, 1, 1), prayed = true, woke = true),
            QiyamLog(date = LocalDate(2025, 1, 2), prayed = false, woke = true),
        )
    )
}