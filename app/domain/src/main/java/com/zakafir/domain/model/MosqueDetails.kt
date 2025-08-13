package com.zakafir.domain.model

data class MosqueDetails(
    val uuid: String,
    val name: String,
    val type: String,
    val slug: String,
    val latitude: Double,
    val longitude: Double,
    val associationName: String? = null,
    val phone: String? = null,
    val paymentWebsite: String? = null,
    val email: String? = null,
    val site: String? = null,
    val closed: String? = null,
    val womenSpace: Boolean? = null,
    val janazaPrayer: Boolean? = null,
    val aidPrayer: Boolean? = null,
    val childrenCourses: Boolean? = null,
    val adultCourses: Boolean? = null,
    val ramadanMeal: Boolean? = null,
    val handicapAccessibility: Boolean? = null,
    val ablutions: Boolean? = null,
    val parking: Boolean? = null,
    val times: List<String> = emptyList(),
    val iqama: List<String> = emptyList(),
    val jumua: String? = null,
    val label: String? = null,
    val localisation: String? = null,
    val image: String? = null,
    val jumua2: String? = null,
    val jumua3: String? = null,
    val jumuaAsDuhr: Boolean? = null,
    val iqamaEnabled: Boolean? = null
) {
    val displayLine: String get() = buildString {
        append("$label - $localisation")
    }
}

