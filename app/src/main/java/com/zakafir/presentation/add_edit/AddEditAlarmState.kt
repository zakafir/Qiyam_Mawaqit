package com.zakafir.presentation.add_edit

import com.zakafir.data.core.domain.ringtone.NameAndUri
import com.zakafir.domain.model.DayValue

data class AddEditAlarmState(
    val hour: String = "",
    val minute: String = "",
    val alarmName: String = "",
    val repeatDays: Set<DayValue> = emptySet(),
    val ringtone: NameAndUri? = null,
    val volume: Float = 0.5f,
    val vibrate: Boolean = true,
    val error: String? = null,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val isDialogOpened: Boolean = false
)
