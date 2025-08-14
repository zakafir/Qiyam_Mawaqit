package com.zakafir.presentation.add_edit

import com.zakafir.data.core.presentation.ui.UiText

interface AddEditAlarmEvent {
    data object OnSuccess: AddEditAlarmEvent
    data class OnFailure(val uiText: UiText): AddEditAlarmEvent
}