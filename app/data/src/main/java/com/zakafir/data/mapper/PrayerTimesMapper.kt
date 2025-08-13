package com.zakafir.data.mapper

import com.zakafir.data.model.MosqueSearchItemDTO
import com.zakafir.domain.model.MosqueDetails

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
        deducedMasjidId = this.deducedMasjidId,
        prayerTimes = this.prayerTimesDTO.map { it.toDomain() }
    )

fun MosqueSearchItemDTO.toDomain(): MosqueDetails =
    MosqueDetails(
        uuid = uuid,
        name = name,
        type = type,
        slug = slug,
        latitude = latitude,
        longitude = longitude,
        associationName = associationName,
        phone = phone,
        paymentWebsite = paymentWebsite,
        email = email,
        site = site,
        closed = closed,
        womenSpace = womenSpace,
        janazaPrayer = janazaPrayer,
        aidPrayer = aidPrayer,
        childrenCourses = childrenCourses,
        adultCourses = adultCourses,
        ramadanMeal = ramadanMeal,
        handicapAccessibility = handicapAccessibility,
        ablutions = ablutions,
        parking = parking,
        times = times,
        iqama = iqama,
        jumua = jumua,
        label = label,
        localisation = localisation,
        image = image,
        jumua2 = jumua2,
        jumua3 = jumua3,
        jumuaAsDuhr = jumuaAsDuhr,
        iqamaEnabled = iqamaEnabled
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
        deducedMasjidId = this.deducedMasjidId,
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