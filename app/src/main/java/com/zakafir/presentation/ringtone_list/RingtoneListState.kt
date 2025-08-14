package com.zakafir.presentation.ringtone_list

import com.zakafir.data.core.domain.ringtone.NameAndUri

data class RingtoneListState(
    val ringtones: List<NameAndUri> = emptyList(),
    val selectedRingtone: NameAndUri? = null
)
