package com.zakafir.domain

import com.zakafir.domain.model.YearlyPrayers

interface LocalDataSource {
    fun readYearlyPrayers(masjidId: String): YearlyPrayers?
    fun writeYearlyPrayers(masjidId: String, yearly: YearlyPrayers)
}
