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

data class PrayerUiState(
    val prayers: Prayers? = null,
    val qiyamWindow: QiyamWindowDTO? = null,
    val isLoading: Boolean = false,
    val error: String? = null
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

            // compute qiyam window regardless of prayers failure (best-effort)
            val qiyam = try {
                repo.computeQiyamWindow(context)
            } catch (t: Throwable) {
                null
            }
            newState = newState.copy(qiyamWindow = qiyam)

            // stop loading
            _uiState.value = newState.copy(isLoading = false)
        }
    }
}
