package com.zakafir.data.local

import android.content.Context
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

    override suspend fun getLocalPrayersTime(masjidId: String): Result<YearlyPrayers> {
        val data = readYearlyPrayers(masjidId)
        return if (data != null) Result.success(data)
        else Result.failure(NoSuchFileException(File(context.filesDir, "$masjidId.json")))
    }

    override fun readYearlyPrayers(masjidId: String): YearlyPrayers? {
        val file = File(context.filesDir, "$masjidId.json")
        if (!file.exists() || file.length() == 0L) return null
        return runCatching {
            val text = file.readText()
            json.decodeFromString<YearlyPrayers>(text)
        }.getOrNull()
    }

    override fun writeYearlyPrayers(
        masjidId: String,
        yearly: YearlyPrayers
    ) {
        val text = runCatching { json.encodeToString(yearly) }.getOrElse { throw it }
        context.openFileOutput("$masjidId.json", Context.MODE_PRIVATE).use { out ->
            out.write(text.toByteArray())
        }
    }

}