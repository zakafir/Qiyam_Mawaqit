package com.zakafir.qiyam_mawaqit.data

import com.zakafir.qiyam_mawaqit.data.model.PrayerTimes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PrayerTimesClient(
    // IMPORTANT: When running on the Android emulator, use 10.0.2.2 to reach the host machine
    private val baseUrl: String = "http://10.0.2.2:8000"
) {
    private val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    /**
     * Defaults to assalam-argenteuil but can be any mosque slug supported by your backend,
     * e.g. "mosquee-bilal-de-waziers-waziers-59119-france".
     */
    suspend fun getPrayerTimes(mosqueSlug: String = "assalam-argenteuil"): PrayerTimes {
        return http.get("$baseUrl/api/v1/$mosqueSlug/prayer-times").body()
    }
}