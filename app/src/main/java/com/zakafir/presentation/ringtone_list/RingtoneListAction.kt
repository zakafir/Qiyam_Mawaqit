package com.zakafir.presentation.ringtone_list

import com.zakafir.data.core.domain.ringtone.NameAndUri

sealed interface RingtoneListAction {
    data class OnRingtoneSelected(val ringtone: NameAndUri): RingtoneListAction
    data object OnBackClick: RingtoneListAction
}