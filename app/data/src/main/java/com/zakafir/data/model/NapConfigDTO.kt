package com.zakafir.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NapConfigDTO(
    val start: String,
    val durationMin: Int
)