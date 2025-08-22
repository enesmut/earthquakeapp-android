package com.enesmut.earthquake.data
import android.os.Build
import com.enesmut.earthquake.domain.Earthquake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


// data/EarthquakeRepository.kt
class EarthquakeRepository(
    private val service: USGSService
) {
    private val iso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC)
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    private fun nowUtc(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        iso.format(Instant.now())
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    private fun hoursAgoUtc(h: Int): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        iso.format(Instant.now().minusSeconds(h * 3600L))
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    suspend fun fetchTurkey(
        hoursWindow: Int,
        ranges: List<Pair<Double, Double>>   // boş => tüm magnitüdler
    ): List<Earthquake> = withContext(Dispatchers.IO) {

        val end = nowUtc()
        val start = hoursAgoUtc(hoursWindow)

        // TEK ağ çağrısı
        val dto = service.getEarthquakesTR(
            startTime = start,
            endTime = end
        )

        val all = dto.features
            .map { it.toDomain() }                           // DTO -> Domain
            .filter { it.latitude != null && it.longitude != null }  // eksik koordinatları at

        // Büyüklük filtrelerini cihazda uygula
        val filtered = if (ranges.isEmpty()) {
            all
        } else {
            all.filter { e ->
                val m = e.magnitude ?: return@filter false
                ranges.any { (a, b) ->
                    if (b == 10.0) m >= a else (m >= a && m < b)
                }
            }
        }

        filtered
            .distinctBy { it.id }
            .sortedByDescending { it.timeMillis ?: 0L }
    }

    // --- EKLENDİ: ViewModel'deki loadByProvince için köprü ---
    // Şimdilik Türkiye BBOX yolunu kullanır; derleme hatasını çözer.
    // İleride USGS circle endpoint hazır olunca burayı gerçek radius çağrısına çevirebiliriz.
    suspend fun fetchAroundProvince(
        hoursWindow: Int,
        lat: Double,
        lon: Double,
        radiusKm: Int,
        ranges: List<Pair<Double, Double>>
    ): List<Earthquake> = withContext(Dispatchers.IO) {

        val end = nowUtc()
        val start = hoursAgoUtc(hoursWindow)

        // tek istek için server-side kabaca daralt (payload küçülür)
        val serverMin = ranges.minOfOrNull { it.first } ?: 0.0
        val serverMax = ranges.maxOfOrNull { it.second } ?: 10.0

        val dto = service.getEarthquakesCircle(
            startTime = start,
            endTime = end,
            latitude = lat,
            longitude = lon,
            radiusKm = radiusKm,
            minMag = serverMin,
            maxMag = serverMax
        )

        val all = dto.features
            .map { it.toDomain() }
            .filter { it.latitude != null && it.longitude != null }

        // kesin filtre (çoklu aralıkları cihazda uygula)
        val filtered = if (ranges.isEmpty()) all else {
            all.filter { e ->
                val m = e.magnitude ?: return@filter false
                ranges.any { (a, b) -> if (b == 10.0) m >= a else (m >= a && m < b) }
            }
        }

        filtered.distinctBy { it.id }
            .sortedByDescending { it.timeMillis ?: 0L }
    }
}