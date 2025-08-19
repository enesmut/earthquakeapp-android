package com.enesmut.earthquake.data
import retrofit2.http.GET
import retrofit2.http.Query
import com.enesmut.earthquake.data.USGSResponseDto
interface USGSService {
    // https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=...&endtime=...
    @GET("query")
    suspend fun getEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTime: String,
        @Query("endtime") endTime: String,
        @Query("minmagnitude") minMagnitude: Double? = null,
        @Query("maxmagnitude") maxMagnitude: Double? = null,
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 200,
        // 🇹🇷 TÜRKİYE sınırları (yaklaşık)
        @Query("minlatitude") minLat: Double = 35.5,
        @Query("maxlatitude") maxLat: Double = 42.5,
        @Query("minlongitude") minLon: Double = 25.5,
        @Query("maxlongitude") maxLon: Double = 45.0
    ): USGSResponseDto
    // RADIUS (Türkiye merkezi + km yarıçapı) — fallback için
    @GET("query")
    suspend fun getEarthquakesCircle(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTime: String,
        @Query("endtime") endTime: String,
        @Query("minmagnitude") minMagnitude: Double? = null,
        @Query("maxmagnitude") maxMagnitude: Double? = null,
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 200,
        @Query("latitude") lat: Double = 39.0,
        @Query("longitude") lon: Double = 35.0,
        @Query("maxradiuskm") radiusKm: Int = 1000
    ): USGSResponseDto
}
