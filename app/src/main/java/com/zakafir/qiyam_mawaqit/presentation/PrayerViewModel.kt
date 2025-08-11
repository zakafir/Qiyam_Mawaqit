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

data class PrayerUiState(
    val prayers: Prayers? = null,
    val qiyamWindow: QiyamWindowDTO? = null,
    val qiyamUiState: QiyamUiState? = null,
    val isLoading: Boolean = false,
    val error: String? = null
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
            val qiyamUiState = qiyamDto?.let { convertToQiyamUi(it, bufferMinutes = 12) }
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
}
