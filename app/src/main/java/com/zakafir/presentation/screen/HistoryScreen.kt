package com.zakafir.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zakafir.domain.model.QiyamLog
import com.zakafir.presentation.component.HistoryRow

@Composable
fun HistoryScreen(
    history: List<QiyamLog>,
) {

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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
            QiyamLog(
                prayed = true, date = "2020-01-01"
            ),
            QiyamLog(
                prayed = true, date = "2020-01-01"
            )
        )
    )
}