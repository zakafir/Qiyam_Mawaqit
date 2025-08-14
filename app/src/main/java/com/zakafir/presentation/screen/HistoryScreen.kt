@file:OptIn(ExperimentalMaterial3Api::class)

package com.zakafir.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zakafir.domain.model.QiyamLog
import com.zakafir.presentation.component.HistoryRow
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.sp

@Composable
fun HistoryScreen(
    weeklyGoal: Int,
    streak: Int,
    history: List<QiyamLog>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onUpdateLog: (date: String, prayed: Boolean) -> Unit,
) {
    val state = rememberPullToRefreshState()
    var editingLog: QiyamLog? by remember { mutableStateOf(null) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                state = state
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
            item {
                StreakHeader(streak = streak, weeklyGoal = weeklyGoal)
            }
            items(history) { item ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingLog = item }
                ) {
                    HistoryRow(item)
                }
            }
        }
        if (editingLog != null) {
            val log = editingLog!!
            AlertDialog(
                onDismissRequest = { editingLog = null },
                title = { Text("Update Qiyam") },
                text = { Text("Set status for ${log.date}") },
                confirmButton = {
                    TextButton(onClick = {
                        onUpdateLog(log.date, true)
                        editingLog = null
                    }) { Text("Mark as Prayed") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onUpdateLog(log.date, false)
                        editingLog = null
                    }) { Text("Mark as Missed") }
                }
            )
        }
    }
}

@Composable
fun StreakHeader(streak: Int, weeklyGoal: Int) {
    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
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

@Preview(showBackground = true)
@Composable
private fun PreviewHistory() {
    HistoryScreen(
        streak = 10,
        weeklyGoal = 15,
        history = listOf(
            QiyamLog(prayed = true, date = "2020-01-01"),
            QiyamLog(prayed = false, date = "2020-01-02")
        ),
        isRefreshing = false,
        onRefresh = {},
        onUpdateLog = { _, _ -> }
    )
}