package com.zakafir.qiyam_mawaqit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakafir.qiyam_mawaqit.data.model.PrayerTimes
import com.zakafir.qiyam_mawaqit.domain.PrayerTimesRepository
import com.zakafir.qiyam_mawaqit.domain.PrayerTimesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class PrayerTimesViewModel(
    private val repo: PrayerTimesRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoadState<PrayerTimes>>(LoadState.Idle)
    val uiState: StateFlow<LoadState<PrayerTimes>> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = LoadState.Loading
        viewModelScope.launch {
            val result = try {
                repo.getAssalamArgenteuil()
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Result.failure(t)
            }
            result.onSuccess { _uiState.value = LoadState.Success(it) }
                .onFailure { _uiState.value = LoadState.Error(it.message ?: "Unknown error", it) }
        }
    }
}
