package com.zakafir.presentation.navigation

import com.zakafir.data.core.domain.ringtone.NameAndUri
import kotlinx.serialization.Serializable

sealed interface RootGraph {

    @Serializable
    data object Home : RootGraph {
        val route: String = "Home"
    }

    @Serializable
    data object History : RootGraph {
        val route: String = "History"
    }

    @Serializable
    data object Settings : RootGraph {
        val route: String = "Settings"
    }

    @Serializable
    data object Details : RootGraph {
        val route: String = "Details"
    }

    @Serializable
    data object AlarmList : RootGraph {
        val route: String = "AlarmList"
    }

    @Serializable
    data class AlarmDetail(val alarmId: String?, val isNewQiyam: Boolean) : RootGraph

    @Serializable
    data class RingtoneList(
        val name: String?,
        val uri: String?,
        val route: String = "RingtoneList"
    ) : RootGraph {
        fun getNameAndUri(): NameAndUri? {
            if (name == null || uri == null) {
                return null
            }
            return Pair(name, uri)
        }
    }
}