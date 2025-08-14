package com.zakafir.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.domain.model.NapConfig
import com.zakafir.domain.repository.PrayerTimesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

class SettingsViewModel(
    private val repo: PrayerTimesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updatePostFajrBuffer(v: Int) {
        _uiState.update { it.copy(postFajrBufferMin = v.coerceIn(0, 120)) }
        viewModelScope.launch { runCatching { repo.updatePostFajrBuffer(v) } }
    }

    fun updateIshaBuffer(v: Int) {
        _uiState.update { it.copy(ishaBufferMin = v.coerceIn(0, 120)) }
        viewModelScope.launch { runCatching { repo.updateIshaBuffer(v) } }
    }

    fun updateMinNightStart(v: String) {
        _uiState.update { it.copy(minNightStart = v) }
        viewModelScope.launch { runCatching { repo.updateMinNightStart(v) } }
    }

    fun updatePostFajrCutoff(v: String) {
        _uiState.update { it.copy(disallowPostFajrIfFajrAfter = v) }
        viewModelScope.launch { runCatching { repo.updatePostFajrCutoff(v) } }
    }

    fun updateNap(index: Int, config: NapConfig) {
        _uiState.update { s ->
            val list = s.naps.toMutableList()
            if (index in list.indices) list[index] = config
            s.copy(naps = list)
        }
        viewModelScope.launch { runCatching { repo.updateNap(index, config) } }
    }

    fun addNap() {
        _uiState.update { s ->
            val current = s.naps
            val nextIndex = current.size + 1
            val defaultStart = when (nextIndex) {
                1 -> "12:00"
                2 -> "16:00"
                3 -> "18:00"
                else -> "16:00"
            }
            val updated = (current + NapConfig(start = defaultStart, durationMin = 0)).take(3)
            s.copy(naps = updated)
        }
        viewModelScope.launch { runCatching { repo.addNap() } }
    }


    fun removeNap(index: Int) {
        _uiState.update { s ->
            val list = s.naps.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            s.copy(naps = list)
        }
        viewModelScope.launch { runCatching { repo.removeNap(index) } }
    }

    fun updateLatestMorningEnd(v: String) {
        _uiState.update { it.copy(latestMorningEnd = v) }
        viewModelScope.launch { runCatching { repo.updateLatestMorningEnd(v) } }
    }


    fun enableNaps(enabled: Boolean) {
        _uiState.update { s -> s.copy(enableNaps = enabled) }
        viewModelScope.launch { runCatching { repo.enableNaps(enabled) } }
    }

    fun enablePostFajr(enabled: Boolean) {
        _uiState.update { s -> s.copy(enablePostFajr = enabled) }
        viewModelScope.launch { runCatching { repo.enablePostFajr(enabled) } }
    }

    fun enableIshaBuffer(enabled: Boolean) {
        _uiState.update { s -> s.copy(enableIshaBuffer = enabled) }
        viewModelScope.launch { runCatching { repo.enableIshaBuffer(enabled) } }
    }

    fun updateWorkStart(it: String) {
        _uiState.update { s -> s.copy(workState = s.workState.copy(workStart = it)) }
        viewModelScope.launch { runCatching { repo.updateWorkStart(it) } }
    }

    fun updateWorkEnd(it: String) {
        _uiState.update { s -> s.copy(workState = s.workState.copy(workEnd = it)) }
        viewModelScope.launch { runCatching { repo.updateWorkEnd(it) } }
    }

    fun updateCommuteToMin(it: Int) {
        _uiState.update { s -> s.copy(workState = s.workState.copy(commuteToMin = it.coerceAtLeast(0))) }
        viewModelScope.launch { runCatching { repo.updateCommuteToMin(it) } }
    }

    fun updateCommuteFromMin(it: Int) {
        _uiState.update { s ->
            s.copy(
                workState = s.workState.copy(
                    commuteFromMin = it.coerceAtLeast(0)
                )
            )
        }
        viewModelScope.launch { runCatching { repo.updateCommuteFromMin(it) } }
    }

    fun updateDesiredSleepHours(v: Float) {
        _uiState.update { it.copy(desiredSleepHours = v.coerceIn(4f, 12f)) }
        viewModelScope.launch { runCatching { repo.updateDesiredSleepHours(v) } }
    }

    fun updateBufferMinutes(v: Int) {
        _uiState.update { it.copy(bufferWuduaMinutes = v.coerceIn(0, 60)) }
        viewModelScope.launch { runCatching { repo.updateBufferMinutes(v) } }
    }

    fun updateAllowPostFajr(allow: Boolean) {
        _uiState.update { it.copy(allowPostFajr = allow) }
        viewModelScope.launch { runCatching { repo.updateAllowPostFajr(allow) } }
    }

    private suspend fun persistAll(state: SettingsUiState) {
        // Simple scalar flags & values
        repo.updatePostFajrBuffer(state.postFajrBufferMin)
        repo.updateIshaBuffer(state.ishaBufferMin)
        repo.updateMinNightStart(state.minNightStart)
        repo.updatePostFajrCutoff(state.disallowPostFajrIfFajrAfter)
        repo.enableNaps(state.enableNaps)
        repo.enablePostFajr(state.enablePostFajr)
        repo.enableIshaBuffer(state.enableIshaBuffer)
        repo.updateLatestMorningEnd(state.latestMorningEnd)
        repo.updateDesiredSleepHours(state.desiredSleepHours)
        repo.updateBufferMinutes(state.bufferWuduaMinutes)
        repo.updateAllowPostFajr(state.allowPostFajr)

        // Work block
        repo.updateWorkStart(state.workState.workStart)
        repo.updateWorkEnd(state.workState.workEnd)
        repo.updateCommuteToMin(state.workState.commuteToMin)
        repo.updateCommuteFromMin(state.workState.commuteFromMin)

        // Naps: normalize repository to exact list in state
        // Strategy: attempt to clear up to 3 stored naps, then add and set
        // (LocalDataSource safely ignores out-of-range removals.)
        repo.removeNap(2)
        repo.removeNap(1)
        repo.removeNap(0)
        state.naps.forEachIndexed { index, nap ->
            repo.addNap()
            repo.updateNap(index, nap)
        }
    }

    fun saveSettings() {
        val snapshot = uiState.value
        viewModelScope.launch { runCatching { persistAll(snapshot) } }
    }

    fun resetDefaults() {
        val defaults = SettingsUiState()
        _uiState.update { defaults }
        viewModelScope.launch { runCatching { persistAll(defaults) } }
    }
}