package com.zakafir.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QiyamLogDTO(val date: String, val prayed: Boolean)