package com.enesmut.earthquake.data

import android.os.Build
import com.enesmut.earthquake.data.toDomain
import com.enesmut.earthquake.data.USGSService
import com.enesmut.earthquake.domain.Earthquake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EarthquakeRepository(
    private val service: USGSService
) {

    private val isoFormatter: DateTimeFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC)
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    private fun nowUtcIso(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        isoFormatter.format(Instant.now())
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    private fun hoursAgoUtcIso(hours: Long): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isoFormatter.format(Instant.now().minusSeconds(hours * 3600))
        } else {
            TODO("VERSION.SDK_INT < O")
        }

    /**
     * @param hoursWindow: 24, 48, 72, 96
     * @param ranges: örn. listOf(0.0 to 2.0, 2.0 to 4.0) gibi büyüklük aralıkları
     *               Eğer boşsa (kullanıcı seçmediyse), tüm magnitüdler.
     */
    suspend fun fetchEarthquakes(
        hoursWindow: Int,
        ranges: List<Pair<Double, Double>> // [min, max)
    ): List<Earthquake> = withContext(Dispatchers.IO) {
        val end = nowUtcIso()
        val start = hoursAgoUtcIso(hoursWindow.toLong())

        // Kullanıcı birden fazla aralık seçtiyse USGS’e ayrı ayrı istek atıp birleştiriyoruz
        val result = if (ranges.isEmpty()) {
            service.getEarthquakes(startTime = start, endTime = end)
                .features.map { it.toDomain() }
        } else {
            ranges.flatMap { (min, max) ->
                service.getEarthquakes(
                    startTime = start,
                    endTime = end,
                    minMagnitude = min,
                    maxMagnitude = max
                ).features.map { it.toDomain() }
            }
        }

        // Aynı deprem birden fazla aralıkta geldiyse id ile uniqle
        result.distinctBy { it.id }
            .sortedByDescending { it.timeMillis ?: 0L }
    }
}