package com.zakafir.presentation.navigation

import com.zakafir.data.core.domain.ringtone.NameAndUri
import com.zakafir.presentation.QiyamAlarm
import kotlinx.serialization.Serializable

sealed interface RootGraph {

    @Serializable
    data object Home : RootGraph

    @Serializable
    data object History : RootGraph

    @Serializable
    data object Settings : RootGraph

    @Serializable
    data object Details : RootGraph

    @Serializable
    data object AlarmList : RootGraph

    @Serializable
    data class AlarmDetail(
        val alarmId: String?,
        val alarmName: String? = null,
        val alarmHour: String? = null,
        val alarmMinute: String? = null,
    ) : RootGraph

    @Serializable
    data class RingtoneList(
        val name: String?,
        val uri: String?,
    ) : RootGraph {
        fun getNameAndUri(): NameAndUri? {
            if (name == null || uri == null) {
                return null
            }
            return Pair(name, uri)
        }
    }
}