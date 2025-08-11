package com.zakafir.qiyam_mawaqit.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.qiyam_mawaqit.data.model.Prayers
import com.zakafir.qiyam_mawaqit.domain.PrayerTimesRepositoryImpl
import com.zakafir.qiyam_mawaqit.domain.QiyamWindowDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
import com.zakafir.qiyam_mawaqit.presentation.screen.NapConfig

data class PrayerUiState(
    val prayers: Prayers? = null,
    val qiyamWindow: QiyamWindowDTO? = null,
    val qiyamUiState: QiyamUiState? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val streak: Int = 11,
    val weeklyGoal: Int = 3,
    // Advanced Sleep Planner controls (values only)
    val desiredSleepHours: Float = 7.5f,
    val postFajrBufferMin: Int = 60,
    val ishaBufferMin: Int = 30,
    val minNightStart: String = "21:30",
    val disallowPostFajrIfFajrAfter: String = "06:30",
    val naps: List<NapConfig> = emptyList(),
    val bufferMinutes: Int = 12,
    val allowPostFajr: Boolean = true,
    val latestMorningEnd: String = "14:00"
)

data class QiyamUiState(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val suggestedWake: LocalDateTime
)

class PrayerTimesViewModel(
    private val repo: PrayerTimesRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    // No init block needed for lambda callbacks

    fun refresh(context: Context) {
        viewModelScope.launch {
            // start loading
            _uiState.update { it.copy(isLoading = true, error = null) }

            // load prayers
            val prayersResult = try {
                repo.getPrayersTime(context)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }

            var newState = _uiState.value
            prayersResult
                .onSuccess { prayers ->
                    newState = newState.copy(prayers = prayers, error = null)
                }
                .onFailure { e ->
                    newState = newState.copy(error = e.message ?: "Unknown error")
                }

            // compute qiyam window and QiyamUiState (best-effort)
            val qiyamDto = try {
                repo.computeQiyamWindow(context)
            } catch (t: Throwable) {
                null
            }
            val qiyamUiState = qiyamDto?.let { convertToQiyamUi(it, bufferMinutes = _uiState.value.bufferMinutes) }
            newState = newState.copy(qiyamWindow = qiyamDto, qiyamUiState = qiyamUiState)

            // stop loading
            _uiState.value = newState.copy(isLoading = false)
        }
    }

    private fun convertToQiyamUi(qiyam: QiyamWindowDTO, bufferMinutes: Int): QiyamUiState {
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
        _uiState.update { it.copy(bufferMinutes = v.coerceIn(0, 120)) }
        recomputeQiyamUiFromState()
    }

    private fun recomputeQiyamUiFromState() {
        val current = _uiState.value
        val updated = current.copy(
            qiyamUiState = current.qiyamWindow?.let { convertToQiyamUi(it, bufferMinutes = current.bufferMinutes) }
        )
        _uiState.value = updated
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

    fun updateWeeklyGoal(v: Int) {
        _uiState.update { it.copy(weeklyGoal = v.coerceIn(0, 7)) }
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
}
