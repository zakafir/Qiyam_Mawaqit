package com.zakafir.data.mapper

import com.zakafir.data.model.PrayerTimesDTO
import com.zakafir.data.model.PrayersDTO
import com.zakafir.data.model.QiyamWindowDTO
import com.zakafir.domain.model.PrayerTimes
import com.zakafir.domain.model.YearlyPrayers
import com.zakafir.domain.model.QiyamWindow

// ---------------------------
// DTO -> Domain
// ---------------------------
fun PrayerTimesDTO.toDomain(): PrayerTimes =
    PrayerTimes(
        date = date,
        fajr = fajr,
        sunset = sunset,
        dohr = dohr,
        asr = asr,
        maghreb = maghreb,
        icha = icha
    )

/**
 * If your API returns a wrapper object containing a list of PrayerTimesDTO.
 */
fun PrayersDTO.toDomain(): YearlyPrayers =
    YearlyPrayers(
        prayerTimes = this.prayerTimesDTO.map { it.toDomain() }
    )

/**
 * If your API returns a bare list (no wrapper), you can map it with this helper.
 */
fun List<PrayerTimesDTO>.toDomain(): YearlyPrayers =
    YearlyPrayers(
        prayerTimes = this.map { it.toDomain() }
    )

// ---------------------------
// Domain -> DTO
// ---------------------------
fun PrayerTimes.toData(): PrayerTimesDTO =
    PrayerTimesDTO(
        date = date,
        fajr = fajr,
        sunset = sunset,
        dohr = dohr,
        asr = asr,
        maghreb = maghreb,
        icha = icha
    )

fun YearlyPrayers.toData(): PrayersDTO =
    PrayersDTO(
        prayerTimesDTO = this.prayerTimes.map { it.toData() }
    )

/**
 * Helper to map a domain list back to DTOs, if you need to persist/send it.
 */
fun List<PrayerTimes>.toData(): List<PrayerTimesDTO> = this.map { it.toData() }

fun QiyamWindowDTO.toDomain(): QiyamWindow =
    QiyamWindow(
        start = start,
        end = end
    )