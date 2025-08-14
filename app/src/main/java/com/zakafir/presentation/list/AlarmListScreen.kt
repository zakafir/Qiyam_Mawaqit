@file:OptIn(ExperimentalLayoutApi::class)

package com.zakafir.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zakafir.presentation.util.getDummyAlarm
import com.zakafir.data.core.util.formatHourMinute
import com.zakafir.data.core.util.formatSeconds
import com.zakafir.domain.model.Alarm
import com.zakafir.domain.model.DayValue
import com.zakafir.presentation.ui.theme.Qiyam_MawaqitTheme
import com.zakafir.qiyam_mawaqit.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlarmListScreenRoot(
    navigateToAddEditScreen: (alarmId: String?) -> Unit,
    viewModel: AlarmListViewModel = koinViewModel()
) {

    AlarmListScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                AlarmListAction.OnAddNewAlarmClick -> navigateToAddEditScreen(null)
                is AlarmListAction.OnAlarmClick -> navigateToAddEditScreen(action.id)
                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )
}

@Composable
private fun AlarmListScreen(
    state: AlarmListState,
    onAction: (AlarmListAction) -> Unit
) {
    Scaffold{ padding ->
        if (state.alarmUi.isEmpty()) {
            EmptyAlarmListContent(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp
                )
            ) {
                item {
                    Text(
                        text = "Your Qiyam Alarms",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(state.alarmUi, key = { it.alarm.id }) { alarmUi ->
                    with(alarmUi) {
                        AlarmListItem(
                            alarm = alarm,
                            timeLeftInSeconds = timeLeftInSeconds,
                            onAlarmClick = {
                                onAction(AlarmListAction.OnAlarmClick(alarm.id))
                            },
                            onDeleteAlarmClick = {
                                onAction(AlarmListAction.OnDeleteAlarmClick(alarm.id))
                            },
                            onToggleAlarm = {
                                onAction(AlarmListAction.OnToggleAlarm(alarm))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlarmListContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Your Alarms",
            style = MaterialTheme.typography.titleLarge
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.alarm),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(62.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "It's empty! Add the first alarm so you don't miss an important moment!",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AlarmListItem(
    alarm: Alarm,
    timeLeftInSeconds: Long,
    onAlarmClick: () -> Unit,
    onDeleteAlarmClick: () -> Unit,
    onToggleAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(20.dp)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alarm.name.ifBlank { "Alarm" },
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = {
                        onToggleAlarm()
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = formatHourMinute(alarm.hourTwelve, alarm.minute),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier
                        .alignByBaseline()
                        .clickable(
                            role = Role.Button
                        ) {
                            onAlarmClick()
                        }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (alarm.isMorning) "AM" else "PM",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Spacer(modifier = Modifier.height(if (alarm.enabled) 8.dp else 16.dp))
            if (alarm.enabled) {
                val remainingTimeStr = formatSeconds(timeLeftInSeconds)

                if (remainingTimeStr.isNotBlank()) {
                    Text(
                        text = "Alarm in $remainingTimeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(
                onClick = onDeleteAlarmClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
    }

}

@Preview
@Composable
private fun AlarmListScreenPreview() {
    Qiyam_MawaqitTheme {
        AlarmListScreen(
            state = AlarmListState(
                alarmUi = getDummyAlarms().map {
                    AlarmUi(
                        alarm = it,
                        timeLeftInSeconds = 0,
                    )
                }
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun EmptyAlarmListContentPreview() {
    Qiyam_MawaqitTheme {
        EmptyAlarmListContent(
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun AlarmListItemPreview() {
    Qiyam_MawaqitTheme {
        val alarm = getDummyAlarms()[0]

        AlarmListItem(
            alarm = alarm,
            timeLeftInSeconds = 0,
            onAlarmClick = {},
            onDeleteAlarmClick = {},
            onToggleAlarm = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getDummyAlarms() = listOf(
    getDummyAlarm(name = "Wake Up", hour = 10, minute = 0, enabled = true),
    getDummyAlarm(name = "Education", hour = 16, minute = 30, enabled = true),
    getDummyAlarm(name = "Dinner", hour = 18, minute = 45, enabled = false)
)