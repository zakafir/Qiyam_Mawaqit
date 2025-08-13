package com.zakafir.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.model.PrayerTimes
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
            // Preload last selected masjid id if available, then refresh
            runCatching { repo.getLastSelectedMasjidId() }
                .getOrNull()
                ?.let { last -> if (!last.isNullOrBlank()) _uiState.update { it.copy(masjidId = last) } }
            refresh()
        }
        viewModelScope.launch {
            _masjidQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.length < 2) {
                        _uiState.update {
                            it.copy(searchResults = emptyList())
                        }
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

            val currentMasjidId = _uiState.value.yearlyPrayers?.deducedMasjidId ?: _uiState.value.masjidId

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
                    newState = newState.copy(yearlyPrayers = prayers, masjidId = prayers.deducedMasjidId)
                }
                .onFailure { e ->
                    // Null out prayers on failure so UI shows nothing
                    newState = newState.copy(error = e.message ?: "Unknown error",)
                }
            // stop loading
            _uiState.value = newState.copy()
        }
    }

    fun computeQiyamWindow(
        todaysPrayerTimes: PrayerTimes?,
        tommorowsPrayerTimes: PrayerTimes?
    ) {
        viewModelScope.launch {
            // Fast path for missing inputs
            if (todaysPrayerTimes == null || tommorowsPrayerTimes == null) {
                _uiState.update { s ->
                    s.copy(
                        qiyamWindow = null,
                        qiyamUiState = null,
                        error = "Missing prayer times for today or tomorrow."
                    )
                }
                return@launch
            }

            // Compute Qiyam window
            val qiyamResult = try {
                repo.computeQiyamWindow(todaysPrayerTimes, tommorowsPrayerTimes)
            } catch (t: Throwable) {
                Result.failure(t)
            }

            qiyamResult
                .onSuccess { qiyam ->
                    _uiState.update { s ->
                        val ui = convertToQiyamUi(qiyam, bufferMinutes = s.bufferMinutes)
                        s.copy(qiyamWindow = qiyam, qiyamUiState = ui, error = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update { s ->
                        s.copy(
                            qiyamWindow = null,
                            qiyamUiState = null,
                            error = e.message ?: "Failed to compute Qiyam window."
                        )
                    }
                }
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
        if (v.isBlank()) {
            // Reset everything to initial state and stop showing any data
            _uiState.value = PrayerUiState()
            _masjidQuery.value = ""
            return
        }
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
        viewModelScope.launch { runCatching { repo.saveLastSelectedMasjidId(slug) } }
        // Trigger a full refresh for the new masjid
        refresh()
    }
}
