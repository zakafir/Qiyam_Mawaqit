package com.zakafir.presentation.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.domain.model.Alarm
import com.zakafir.domain.repository.AlarmRepository
import com.zakafir.domain.usecase.GetFutureDateUseCase
import com.zakafir.domain.usecase.GetTimeLeftInSecondsUseCase
import com.zakafir.domain.usecase.GetTimeToSleepInSecondsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Suppress("OPT_IN_USAGE")
class AlarmListViewModel(
    private val alarmRepository: AlarmRepository,
    private val getFutureDateUseCase: GetFutureDateUseCase,
    private val getTimeLeftInSecondsUseCase: GetTimeLeftInSecondsUseCase,
    private val getTimeToSleepInSecondsUseCase: GetTimeToSleepInSecondsUseCase
): ViewModel() {

    var state by mutableStateOf(AlarmListState())
        private set

    init {
        alarmRepository
            .getAll()
            .flatMapLatest { alarms ->
                if (alarms.isEmpty()) {
                    return@flatMapLatest flowOf(emptyList<AlarmUi>())
                }
                val alarmUiFlows = alarms
                    .sortedWith(compareBy<Alarm> { it.hour }.thenBy { it.minute })
                    .map { alarm ->
                    val futureDateTime = getFutureDateUseCase(alarm.hour, alarm.minute, alarm.repeatDays)
                    val timeLeftInSecondsFlow: Flow<Long> = getTimeLeftInSecondsUseCase(futureDateTime)
                    val timeToSleepInSecondsFlow: Flow<Long?> = getTimeToSleepInSecondsUseCase(alarm.hour, futureDateTime)

                    combine(timeLeftInSecondsFlow, timeToSleepInSecondsFlow) { timeLeft, timeToSleep ->
                        AlarmUi(
                            alarm = alarm,
                            timeLeftInSeconds = timeLeft,
                        )
                    }
                }

                combine(alarmUiFlows) { it.toList() }
            }.onEach { alarms ->
                state = state.copy(alarmUi = alarms)
            }.launchIn(viewModelScope)
    }

    fun onAction(action: AlarmListAction) {
        when (action) {
            is AlarmListAction.OnToggleAlarm -> {
                viewModelScope.launch {
                    alarmRepository.toggle(action.alarm)
                }
            }
            is AlarmListAction.OnDeleteAlarmClick -> {
                viewModelScope.launch {
                    alarmRepository.deleteById(action.id)
                }
            }
            is AlarmListAction.OnToggleDayOfAlarm -> {
                viewModelScope.launch {
                    alarmRepository.toggleDay(action.day, action.alarm)
                }
            }
            else -> Unit
        }
    }
}