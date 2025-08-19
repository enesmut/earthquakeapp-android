package com.enesmut.earthquake.data



import android.os.Build
import android.util.Log
import com.enesmut.earthquake.domain.Earthquake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EarthquakeRepository(
    private val service: USGSService
) {
    private val TAG = "EQRepo"

    // --- Zaman formatlayıcı (UTC ISO8601) ---
    private val isoFormatter: DateTimeFormatter =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
        } else {
            // Eğer desugaring açıksa bu branşı sadeleştirebilirsin
            throw IllegalStateException("Requires Java 8+ time API")
        }

    private fun nowUtcIso(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) isoFormatter.format(Instant.now())
        else throw IllegalStateException("Requires Java 8+ time API")

    private fun hoursAgoUtcIso(hours: Long): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            isoFormatter.format(Instant.now().minusSeconds(hours * 3600))
        else throw IllegalStateException("Requires Java 8+ time API")

    // --- TÜRKİYE sınır kutusu (ek güvenlik için client-side filtre) ---
    private val MIN_LAT = 35.5
    private val MAX_LAT = 42.5
    private val MIN_LON = 25.5
    private val MAX_LON = 45.0

    private fun isInTurkey(lat: Double?, lon: Double?): Boolean {
        if (lat == null || lon == null) return false
        return lat in MIN_LAT..MAX_LAT && lon in MIN_LON..MAX_LON
    }

    /**
     * UI'dan gelen saat penceresi ve büyüklük aralıklarına göre veri çeker.
     *
     * Strateji (yalnızca TR):
     *  1) Seçilen saat penceresi + TR BBOX
     *  2) Seçilen saat penceresi + TR RADIUS (1000 km)
     *  3) 7 gün (168 saat) + TR BBOX
     *  4) 30 gün (720 saat) + TR BBOX
     *
     * Not: GLOBAL plan **YOK**; ayrıca sonunda client-side TR filtresi var.
     */
    suspend fun fetchEarthquakes(
        hoursWindow: Int,
        ranges: List<Pair<Double, Double>>
    ): List<Earthquake> = withContext(Dispatchers.IO) {

        val plans = listOf(
            Plan(hoursWindow, Geo.BBOX_TR),
            Plan(hoursWindow, Geo.RADIUS_TR),
            Plan(168, Geo.BBOX_TR),   // 7 gün
            Plan(720, Geo.BBOX_TR)    // 30 gün
        )

        for (plan in plans) {
            val end = nowUtcIso()
            val start = hoursAgoUtcIso(plan.hours.toLong())

            // USGS çağrısı
            val features = if (ranges.isEmpty()) {
                call(plan.geo, start, end, null, null)
            } else {
                ranges.flatMap { (min, max) -> call(plan.geo, start, end, min, max) }
            }

            // DTO -> Domain + kesin TR filtresi + uniq + tarihçe
            val list = features
                .map { it.toDomain() }                             // FeatureDto -> Earthquake
                .filter { isInTurkey(it.latitude, it.longitude) }  // kesin TR çiti
                .distinctBy { it.id }
                .sortedByDescending { it.timeMillis ?: 0L }

            Log.d(TAG, "plan=${plan.geo} window=${plan.hours}h -> ${list.size} kayıt")

            if (list.isNotEmpty()) return@withContext list
        }

        emptyList()
    }

    // --- Servis çağrıları (BBOX / RADIUS) ---
    private suspend fun call(
        geo: Geo,
        start: String,
        end: String,
        minMag: Double?,
        maxMag: Double?
    ): List<FeatureDto> {
        return when (geo) {
            Geo.BBOX_TR -> service.getEarthquakes(
                startTime = start,
                endTime = end,
                minMagnitude = minMag,
                maxMagnitude = maxMag
                // TR BBOX parametreleri servis fonksiyonunda default
            ).features

            Geo.RADIUS_TR -> service.getEarthquakesCircle(
                startTime = start,
                endTime = end,
                minMagnitude = minMag,
                maxMagnitude = maxMag
                // TR radius parametreleri servis fonksiyonunda default
            ).features
        }
    }

    private data class Plan(val hours: Int, val geo: Geo)
    private enum class Geo { BBOX_TR, RADIUS_TR }
}