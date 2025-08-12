package com.zakafir.data

import com.zakafir.data.model.MosqueSearchItemDTO
import com.zakafir.data.model.PrayerTimesDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

/**
 * Thin wrapper around the public Mawaqit proxy API at
 * https://mawaqit-api.up.railway.app/docs
 */
class PrayerTimesApi {
    private val baseUrl: String = "https://mawaqit-api.up.railway.app/api/v1"

    /** A shared Ktor client with JSON + logging */
    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    @Serializable
    data class YearCalendarDTO(
        // Index 0..11 => months Jan..Dec
        // Map key is day "1".."31", value is [fajr, sunset, dohr, asr, maghreb, icha]
        val calendar: List<Map<String, List<String>>>
    )

    /**
     * GET /{masjidId}/calendar
     * Returns the whole year's calendar for the given masjid.
     */
    suspend fun getYearlyCalendar(
        masjidId: String
    ): YearCalendarDTO {
        val url = URLBuilder(baseUrl)
            .apply { appendPathSegments(masjidId, "calendar") }
            .buildString()
        return client.get(url).body()
    }

    /**
     * GET https://mawaqit.net/api/2.0/mosque/search?word=...
     * Remote search of mosques on Mawaqit. This uses the public Mawaqit host (not the proxy baseUrl).
     */
    suspend fun searchMosques(word: String): List<MosqueSearchItemDTO> {
        if (word.isBlank()) return emptyList()
        return client.get("https://mawaqit.net/api/2.0/mosque/search") {
            url.parameters.append("word", word)
        }.body()
    }
}