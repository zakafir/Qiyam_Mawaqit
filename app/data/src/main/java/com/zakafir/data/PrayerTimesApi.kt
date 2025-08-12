package com.zakafir.data

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

    /** Default masjid id you can reuse from the app if none specified */
    companion object {
        const val DEFAULT_MASJID_ID: String =
            "mosquee-bilal-de-waziers-waziers-59119-france"
    }

    /**
     * GET /{masjidId}/prayer-times
     * Returns today's prayer times for the given masjid.
     */
    suspend fun getPrayerTimes(
        masjidId: String = DEFAULT_MASJID_ID
    ): PrayerTimesDTO {
        val url = URLBuilder(baseUrl)
            .apply { appendPathSegments(masjidId, "prayer-times") }
            .buildString()
        return client.get(url).body()
    }

    /**
     * GET /{masjidId}/calendar/{month}
     * Returns a list of daily prayer times for the given month (1..12).
     */
    suspend fun getMonthlyCalendar(
        masjidId: String = DEFAULT_MASJID_ID,
        month: Int
    ): List<PrayerTimesDTO> {
        require(month in 1..12) { "month must be in 1..12, was $month" }
        val url = URLBuilder(baseUrl)
            .apply { appendPathSegments(masjidId, "calendar", month.toString()) }
            .buildString()
        return client.get(url).body()
    }

    // Inside PrayerTimesApi.kt (same package and imports as you already have)

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
        masjidId: String = DEFAULT_MASJID_ID
    ): YearCalendarDTO {
        val url = URLBuilder(baseUrl)
            .apply { appendPathSegments(masjidId, "calendar") }
            .buildString()
        return client.get(url).body()
    }

    /** Optional: close underlying resources. Call from DI shutdown if needed. */
    suspend fun close() {
        client.close()
    }
}