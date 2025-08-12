package com.zakafir.data.local

import android.content.Context
import com.zakafir.data.mapper.toData
import com.zakafir.data.mapper.toDomain
import com.zakafir.data.model.PrayersDTO
import com.zakafir.domain.LocalDataSource
import com.zakafir.domain.model.YearlyPrayers
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class LocalDataSourceImpl(
    private val context: Context,
): LocalDataSource {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun readYearlyPrayers(masjidId: String): YearlyPrayers? {
        val file = File(context.filesDir, "$masjidId.json")
        val targetFile = if (!file.exists() || file.length() == 0L) {
            // Get the most recently modified .json file in the files directory
            context.filesDir.listFiles { f -> f.extension == "json" }
                ?.filter { it.length() > 0 }
                ?.maxByOrNull { it.lastModified() }
        } else file

        if (targetFile == null || targetFile.length() == 0L) return null
        return runCatching {
            val text = targetFile.readText()
            json.decodeFromString<PrayersDTO>(text).toDomain()
        }.getOrNull()
    }

    override fun writeYearlyPrayers(
        masjidId: String,
        yearly: YearlyPrayers
    ) {
        val dto = yearly.toData()
        val text = runCatching { json.encodeToString(dto) }.getOrElse { throw it }
        context.openFileOutput("$masjidId.json", Context.MODE_PRIVATE).use { out ->
            out.write(text.toByteArray())
        }
    }

}