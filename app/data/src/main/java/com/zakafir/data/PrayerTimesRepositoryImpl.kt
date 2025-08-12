package com.zakafir.data

import android.content.Context
import com.zakafir.data.mapper.toDomain
import com.zakafir.data.model.PrayerTimesDTO
import com.zakafir.data.model.PrayersDTO
import com.zakafir.data.model.QiyamWindowDTO
import com.zakafir.domain.PrayerTimesRepository
import com.zakafir.domain.model.Prayers
import com.zakafir.domain.model.QiyamWindow
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

class PrayerTimesRepositoryImpl(
    private val context: Context,
    private val api: PrayerTimesApi
) : PrayerTimesRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private var yearly: List<PrayerTimesDTO>? = null

    override suspend fun getPrayersTime(masjidId: String): Result<Prayers> = runCatching {
        val today = Calendar.getInstance()
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        PrayersDTO(
            listOf(
                ptFor(cal = today, masjidId = masjidId),
                ptFor(cal = tomorrow, masjidId = masjidId)
            )
        ).toDomain()
    }

    override suspend fun computeQiyamWindow(masjidId: String): QiyamWindow {
        val today = Calendar.getInstance()
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val t = ptFor(cal = today, masjidId = masjidId)
        val n = ptFor(cal = tomorrow, masjidId = masjidId)

        val start = (today.clone() as Calendar).apply { setHM(t.icha) }
        val end = (tomorrow.clone() as Calendar).apply { setHM(n.fajr) }
        val endMs = if (end.timeInMillis <= start.timeInMillis) end.apply {
            add(
                Calendar.DAY_OF_MONTH,
                1
            )
        }.timeInMillis else end.timeInMillis
        val lastThirdMs = start.timeInMillis + (endMs - start.timeInMillis) * 2 / 3

        return QiyamWindowDTO(
            start = (today.clone() as Calendar).apply { timeInMillis = lastThirdMs }.hhmm(),
            end = (today.clone() as Calendar).apply { timeInMillis = endMs }.hhmm()
        ).toDomain()
    }

    private suspend fun ptFor(cal: Calendar, masjidId: String): PrayerTimesDTO {
        val list = ensureYearly(masjidId)
        println(
            "Yearly calendar stored at: ${
                File(
                    context.filesDir,
                    fileName(masjidId)
                ).absolutePath
            }"
        )
        val y = cal.get(Calendar.YEAR);
        val m = cal.get(Calendar.MONTH) + 1;
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val date = "%04d-%02d-%02d".format(Locale.FRANCE, y, m, d)
        return list.firstOrNull { it.date == date }?.let { it } ?: run {
            val idx = (cal.get(Calendar.DAY_OF_YEAR) - 1).coerceIn(0, list.lastIndex)
            list[idx].copy(date = date)
        }
    }

    private suspend fun ensureYearly(masjidId: String): List<PrayerTimesDTO> {
        yearly?.let { return it }
        val file = File(context.filesDir, fileName(masjidId))

        // 1) filesDir
        readTextOrNull(file)?.let { parseYearly(it).also { p -> yearly = p; return p } }
        // 2) assets
        readAssetOrNull(context, fileName(masjidId))?.let { text ->
            val parsed = parseYearly(text)
            writeCanonical(file, parsed)
            yearly = parsed
            return parsed
        }
        // 3) fetch & persist
        val raw = runCatching { api.getYearlyCalendar(masjidId) }.getOrElse { throw it }
        val parsed = when (raw) {
            is String -> parseYearly(raw)
            else -> {
                parseYearly(json.encodeToJsonElement(raw).toString())
            }
        }
        writeCanonical(file, parsed)
        yearly = parsed
        return parsed
    }

    private fun parseYearly(text: String): List<PrayerTimesDTO> =
        when (val root = json.parseToJsonElement(text)) {
            is JsonArray -> root.mapNotNull { it.objOrNull()?.toPrayerTimes() }
            is JsonObject -> root.calendarToList()
            else -> emptyList()
        }

    private fun JsonObject.calendarToList(): List<PrayerTimesDTO> {
        val out = mutableListOf<PrayerTimesDTO>()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val months = this["calendar"] as? JsonArray ?: return emptyList()
        months.forEachIndexed { mIdx, monthEl ->
            (monthEl as? JsonObject)?.entries
                ?.sortedBy { it.key.toIntOrNull() ?: Int.MAX_VALUE }
                ?.forEach { (dayKey, v) ->
                    val day = dayKey.toIntOrNull() ?: return@forEach
                    val times =
                        (v as? JsonArray)?.map { it.jsonPrimitive.content } ?: return@forEach
                    out += PrayerTimesDTO(
                        fajr = times.getOrNull(0) ?: "00:00",
                        sunset = times.getOrNull(1)
                            ?: "00:00", // API gives sunrise; model uses 'sunset'
                        dohr = times.getOrNull(2) ?: "00:00",
                        asr = times.getOrNull(3) ?: "00:00",
                        maghreb = times.getOrNull(4) ?: "00:00",
                        icha = times.getOrNull(5) ?: "00:00",
                        date = "%04d-%02d-%02d".format(Locale.FRANCE, year, mIdx + 1, day)
                    )
                }
        }
        return out
    }

    private fun JsonObject.toPrayerTimes(): PrayerTimesDTO? = runCatching {
        PrayerTimesDTO(
            fajr = str("fajr"),
            sunset = strOrNull("sunset") ?: strOrNull("sunrise") ?: "00:00",
            dohr = str("dohr"),
            asr = str("asr"),
            maghreb = str("maghreb"),
            icha = str("icha"),
            date = strOrNull("date") ?: ""
        )
    }.getOrNull()

    private fun fileName(masjidId: String) = "$masjidId.json"
    private fun readTextOrNull(file: File): String? =
        runCatching { if (file.exists() && file.length() > 0) file.readText() else null }.getOrNull()

    private fun readAssetOrNull(context: Context, name: String): String? =
        runCatching { context.assets.open(name).bufferedReader().use { it.readText() } }.getOrNull()

    private fun writeCanonical(file: File, list: List<PrayerTimesDTO>) =
        file.writeText(json.encodeToString(ListSerializer(PrayerTimesDTO.serializer()), list))

    // ---- Tiny helpers ----
    private fun Calendar.setHM(hm: String) {
        val (h, m) = hm.split(':').mapNotNull { it.toIntOrNull() }
            .let { (it.getOrNull(0) ?: 0) to (it.getOrNull(1) ?: 0) }; set(
            Calendar.HOUR_OF_DAY,
            h
        ); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private fun Calendar.hhmm(): String =
        "%02d:%02d".format(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))

    private fun JsonElement.objOrNull(): JsonObject? = this as? JsonObject
    private fun JsonObject.str(key: String): String =
        this[key]?.jsonPrimitive?.content ?: error("Missing $key")

    private fun JsonObject.strOrNull(key: String): String? = this[key]?.jsonPrimitive?.content
}