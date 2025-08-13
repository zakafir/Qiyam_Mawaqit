package com.zakafir.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.QiyamMode
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

    // Cache the last inputs used for Qiyam computation so we can recompute when mode changes
    private var lastTodays: PrayerTimes? = null
    private var lastTomorrows: PrayerTimes? = null

    // Selected Qiyam mode lives in the UI state inside QiyamUiState, but we keep a local
    // fallback for when there is no QiyamUiState yet (first computation)
    private var selectedQiyamMode: QiyamMode = QiyamMode.LastThird

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
                        _uiState.update { it.copy(searchResults = emptyList()) }
                    } catch (c: Throwable) {
                        print(c.message)
                        _uiState.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // start loading & clear previous error
            _uiState.update { it.copy(isLoading = true) }

            val currentMasjidId =
                _uiState.value.yearlyPrayers?.deducedMasjidId ?: _uiState.value.masjidId

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
                    newState =
                        newState.copy(yearlyPrayers = prayers, masjidId = prayers.deducedMasjidId)
                }
                .onFailure { e ->
                    // Null out prayers on failure so UI shows nothing
                    newState = newState.copy(error = e.message ?: "Unknown error")
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
            // Cache inputs for later recomputation when mode changes
            lastTodays = todaysPrayerTimes
            lastTomorrows = tommorowsPrayerTimes

            // Fast path for missing inputs
            if (todaysPrayerTimes == null || tommorowsPrayerTimes == null) {
                _uiState.update { s ->
                    s.copy(
                        qiyamUiState = null,
                        error = "Missing prayer times for today or tomorrow."
                    )
                }
                return@launch
            }

            val mode = _uiState.value.qiyamUiState?.mode ?: selectedQiyamMode

            // Compute Qiyam window with the selected mode
            val qiyamResult = try {
                repo.computeQiyamWindow(mode, todaysPrayerTimes, tommorowsPrayerTimes)
            } catch (t: Throwable) {
                Result.failure(t)
            }

            qiyamResult
                .onSuccess { qiyam ->
                    _uiState.update { s ->
                        val ui =
                            convertToQiyamUi(qiyam, mode = mode, bufferMinutes = s.bufferMinutes)
                        s.copy(qiyamUiState = ui, error = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update { s ->
                        s.copy(
                            qiyamUiState = null,
                            error = e.message ?: "Failed to compute Qiyam window."
                        )
                    }
                }
        }
    }

    private fun convertToQiyamUi(
        qiyam: QiyamWindow,
        mode: QiyamMode,
        bufferMinutes: Int
    ): QiyamUiState {
        val today: LocalDate =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun parseHourMinute(timeStr: String): Pair<Int, Int> {
            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return hour to minute
        }

        val (startHour, startMinute) = parseHourMinute(qiyam.start)
        val startDateTime = LocalDateTime(
            today.year,
            today.monthNumber,
            today.dayOfMonth,
            startHour,
            startMinute,
            0,
            0
        )

        val (endHour, endMinute) = parseHourMinute(qiyam.end)
        var endCandidate =
            LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, endHour, endMinute, 0, 0)

        val tz = TimeZone.currentSystemDefault()
        val startInstant = startDateTime.toInstant(tz)
        var endInstant = endCandidate.toInstant(tz)
        if (endInstant <= startInstant) endInstant = endInstant.plus(1.days)
        val endDateTime = endInstant.toLocalDateTime(tz)

        // Suggested wake = a few minutes before the start (buffer)
        val suggestedWake = startInstant.minus(bufferMinutes.minutes).toLocalDateTime(tz)

        // Duration as string (handles overnight)
        val durationMinutes = ((endInstant.epochSeconds - startInstant.epochSeconds) / 60).toInt()
        val durH = durationMinutes / 60
        val durM = durationMinutes % 60
        val durationStr = String.format("%dh %02dm", durH, durM)

        return QiyamUiState(
            start = qiyam.start,
            end = qiyam.end,
            duration = durationStr,
            mode = mode,
            window = qiyam,
            suggestedWake = suggestedWake,
        )
    }

    fun updateDesiredSleepHours(v: Float) {
        _uiState.update { it.copy(desiredSleepHours = v.coerceIn(4f, 12f)) }
    }

    fun updatePostFajrBuffer(v: Int) {
        _uiState.update { it.copy(postFajrBufferMin = v.coerceIn(0, 120)) }
    }

    fun updateIshaBuffer(v: Int) {
        _uiState.update { it.copy(ishaBufferMin = v.coerceIn(0, 120)) }
    }

    fun updateMinNightStart(v: String) {
        _uiState.update { it.copy(minNightStart = v) }
    }

    fun updatePostFajrCutoff(v: String) {
        _uiState.update { it.copy(disallowPostFajrIfFajrAfter = v) }
    }

    fun updateNap(index: Int, config: NapConfig) {
        _uiState.update { s ->
            val list = s.naps.toMutableList()
            if (index in list.indices) list[index] = config
            s.copy(naps = list)
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
            s.copy(naps = updated)
        }
    }

    fun removeNap(index: Int) {
        _uiState.update { s ->
            val list = s.naps.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            s.copy(naps = list)
        }
    }

    fun updateLatestMorningEnd(v: String) {
        _uiState.update { it.copy(latestMorningEnd = v) }
    }

    fun updateMasjidId(v: String) {
        if (v.isBlank()) {
            // Reset everything to initial state and stop showing any data
            _uiState.value = PrayerUiState()
            _masjidQuery.value = ""
            return
        }
        _uiState.update { it.copy(masjidId = v) }
        _masjidQuery.value = v
    }

    fun selectMasjidSuggestion(slug: String?) {
        if (slug == null || slug.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a Masjid ID") }
            return
        }
        _uiState.update {
            it.copy(
                masjidId = slug,
                selectedMosque = it.searchResults.find { m -> m.slug == slug }
            )
        }
        viewModelScope.launch { runCatching { repo.saveLastSelectedMasjidId(slug) } }
        // Trigger a full refresh for the new masjid
        refresh()
    }

    fun updateQiyamMode(it: QiyamMode) {
        selectedQiyamMode = it

        // Update mode in existing UI state if present
        _uiState.update { s ->
            val cur = s.qiyamUiState
            if (cur != null) s.copy(qiyamUiState = cur.copy(mode = it)) else s
        }

        // Recompute instantly if we have cached inputs
        val t = lastTodays
        val tm = lastTomorrows
        if (t != null && tm != null) {
            viewModelScope.launch {
                val res = try {
                    repo.computeQiyamWindow(it, t, tm)
                } catch (thr: Throwable) {
                    Result.failure(thr)
                }
                res
                    .onSuccess { q ->
                        _uiState.update { s ->
                            val ui = convertToQiyamUi(q, mode = it, bufferMinutes = s.bufferMinutes)
                            s.copy(qiyamUiState = ui, error = null)
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { s -> s.copy(qiyamUiState = null, error = e.message) }
                    }
            }
        }
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
        _uiState.update { s -> s.copy(workState = s.workState.copy(commuteFromMin = it.coerceAtLeast(0))) }
        viewModelScope.launch { runCatching { repo.updateCommuteFromMin(it) } }
    }
    fun resetData() {
        _uiState.value = PrayerUiState()
        _masjidQuery.value = ""
        lastTodays = null
        lastTomorrows = null
        selectedQiyamMode = QiyamMode.LastThird
    }
}
