package com.zakafir.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.model.QiyamWindow
import com.zakafir.presentation.screen.NapConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.toInstant
import kotlin.collections.plus

class PrayerTimesViewModel(
    private val repo: PrayerTimesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()
    private val _masjidQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _masjidQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.length < 2) {
                        _uiState.update { PrayerUiState() }
                        return@collect
                    }
                    try {
                        val results = repo.searchMosques(q)
                        _uiState.update { it.copy(searchResults = results) }
                    } catch (c: CancellationException) {
                        print(c.message)
                        _uiState.update { it.copy(searchResults = emptyList(),) }
                    } catch (c: Throwable) {
                        print(c.message)
                        _uiState.update { it.copy(searchResults = emptyList(),) }
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // start loading & clear previous error
            _uiState.update { it.copy(isLoading = true,) }

            val currentMasjidId = _uiState.value.masjidId

            // 1) Load prayers as Result
            val prayersResult = try {
                repo.getPrayersTime(currentMasjidId)
            } catch (ce: CancellationException) {
                Result.failure(ce)
            } catch (t: Throwable) {
                Result.failure(t)
            }

            var newState = _uiState.value
            prayersResult
                .onSuccess { prayers ->
                    newState = newState.copy(yearlyPrayers = prayers,)
                }
                .onFailure { e ->
                    // Null out prayers on failure so UI shows nothing
                    newState = newState.copy(error = e.message ?: "Unknown error",)
                }

            // 2) Compute Qiyam window as Result
            val qiyamResult = try {
                repo.computeQiyamWindow(currentMasjidId)
            } catch (t: Throwable) {
                Result.failure(t)
            }

            qiyamResult
                .onSuccess { qiyam ->
                    val ui = convertToQiyamUi(qiyam, bufferMinutes = newState.bufferMinutes)
                    newState = newState.copy(qiyamWindow = qiyam, qiyamUiState = ui,)
                }
                .onFailure { e ->
                    // Null out Qiyam data on failure so UI shows nothing
                    newState = newState.copy(error = newState.error ?: (e.message ?: "Unknown error"),)
                }

            // stop loading
            _uiState.value = newState.copy()
        }
    }

    private fun convertToQiyamUi(qiyam: QiyamWindow, bufferMinutes: Int): QiyamUiState {
        val today: LocalDate =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val (startHour, startMinute) = parseHourMinute(qiyam.start)
        val startDateTime = LocalDateTime(
            year = today.year,
            monthNumber = today.monthNumber,
            dayOfMonth = today.dayOfMonth,
            hour = startHour,
            minute = startMinute,
            second = 0,
            nanosecond = 0
        )

        val (endHour, endMinute) = parseHourMinute(qiyam.end)
        // Build endCandidate on the same day
        var endCandidate = LocalDateTime(
            year = today.year,
            monthNumber = today.monthNumber,
            dayOfMonth = today.dayOfMonth,
            hour = endHour,
            minute = endMinute,
            second = 0,
            nanosecond = 0
        )
        val tz = TimeZone.currentSystemDefault()
        val startInstant = startDateTime.toInstant(tz)
        var endInstant = endCandidate.toInstant(tz)
        if (endInstant <= startInstant) {
            endInstant = endInstant.plus(1.days)
        }
        val endDateTime = endInstant.toLocalDateTime(tz)

        val suggestedWake =
            startDateTime.toInstant(tz).minus(bufferMinutes.minutes).toLocalDateTime(tz)

        return QiyamUiState(
            start = startDateTime,
            end = endDateTime,
            suggestedWake = suggestedWake
        )
    }

    private fun parseHourMinute(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour to minute
    }

    fun updateBuffer(v: Int) {
        _uiState.update { it.copy(bufferMinutes = v.coerceIn(0, 120),) }
        recomputeQiyamUiFromState()
    }

    private fun recomputeQiyamUiFromState() {
        val current = _uiState.value
        val updated = current.copy(
            qiyamUiState = current.qiyamWindow?.let {
                convertToQiyamUi(
                    it,
                    bufferMinutes = current.bufferMinutes
                )
            },
        )
        _uiState.value = updated
    }

    fun updateDesiredSleepHours(v: Float) {
        _uiState.update { it.copy(desiredSleepHours = v.coerceIn(4f, 12f),) }
    }

    fun updatePostFajrBuffer(v: Int) {
        _uiState.update { it.copy(postFajrBufferMin = v.coerceIn(0, 120),) }
    }

    fun updateIshaBuffer(v: Int) {
        _uiState.update { it.copy(ishaBufferMin = v.coerceIn(0, 120),) }
    }

    fun updateWeeklyGoal(v: Int) {
        _uiState.update { it.copy(weeklyGoal = v.coerceIn(0, 7),) }
    }

    fun updateMinNightStart(v: String) {
        _uiState.update { it.copy(minNightStart = v,) }
    }

    fun updatePostFajrCutoff(v: String) {
        _uiState.update { it.copy(disallowPostFajrIfFajrAfter = v,) }
    }

    fun updateNap(index: Int, config: NapConfig) {
        _uiState.update { s ->
            val list = s.naps.toMutableList()
            if (index in list.indices) list[index] = config
            s.copy(naps = list,)
        }
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
            s.copy(naps = updated,)
        }
    }

    fun removeNap(index: Int) {
        _uiState.update { s ->
            val list = s.naps.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            s.copy(naps = list,)
        }
    }

    fun updateLatestMorningEnd(v: String) {
        _uiState.update { it.copy(latestMorningEnd = v,) }
    }

    fun updateMasjidId(v: String) {
        _uiState.update { it.copy(masjidId = v,) }
        _masjidQuery.value = v
    }

    fun selectMasjidSuggestion(slug: String?) {
        if (slug == null || slug.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a Masjid ID",) }
            return
        }
        _uiState.update {
            it.copy(
                masjidId = slug,
                isLoading = true,
            )
        }
        // Trigger a full refresh for the new masjid
        refresh()
    }
}
