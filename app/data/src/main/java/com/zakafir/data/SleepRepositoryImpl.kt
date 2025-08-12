package com.zakafir.data

import android.content.Context
import com.zakafir.data.mapper.toDomain
import com.zakafir.data.model.PrayerTimesDTO
import com.zakafir.data.model.PrayersDTO
import com.zakafir.data.model.QiyamWindowDTO
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.domain.model.QiyamWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.collections.mapNotNull

class SleepRepositoryImpl(
) {

}