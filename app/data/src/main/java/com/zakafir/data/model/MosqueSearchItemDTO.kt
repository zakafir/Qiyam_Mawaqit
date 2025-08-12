package com.zakafir.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MosqueSearchItemDTO(
    @SerialName("_id") val underscoreId: String? = null,
    val id: String? = null,
    val slug: String? = null,
    val name: String? = null,
    val city: String? = null,
    val country: String? = null,
    val label: String? = null,
    val localisation: String? = null,
) {
    val stableId: String get() = id ?: underscoreId ?: slug ?: name.orEmpty()
    val displayLine: String get() = buildString {
        append("$label - $localisation")
        val loc = listOfNotNull(city, country).joinToString(", ")
        if (loc.isNotBlank()) append(" • ").append(loc)
    }
}