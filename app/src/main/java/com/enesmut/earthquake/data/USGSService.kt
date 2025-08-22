package com.enesmut.earthquake.data
import retrofit2.http.GET
import retrofit2.http.Query
import com.enesmut.earthquake.data.USGSResponseDto
// data/remote/USGSService.kt
interface USGSService {

    // --- TÜRKİYE BBOX (mevcut fonksiyonun) ---
    @GET("query")
    suspend fun getEarthquakesTR(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTime: String,
        @Query("endtime") endTime: String,

        // Türkiye için geniş bbox
        @Query("minlatitude") minLat: Double = 34.0,
        @Query("maxlatitude") maxLat: Double = 43.5,
        @Query("minlongitude") minLon: Double = 24.0,
        @Query("maxlongitude") maxLon: Double = 47.0,

        // TÜM magnitüdleri dahil et
        @Query("minmagnitude") minMag: Double = 0.0,

        // sıralama + yeterli sayıda kayıt
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 5000,

        // sadece deprem olayları
        @Query("eventtype") eventType: String = "earthquake"
    ): USGSResponseDto


    // --- ÇEMBER (İL MERKEZİ + RADIUS) ---
    @GET("query")
    suspend fun getEarthquakesCircle(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTime: String,
        @Query("endtime") endTime: String,

        // merkez + yarıçap (km)
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("maxradiuskm") radiusKm: Int,

        // isteğe bağlı server-side daraltma (payload küçülür)
        @Query("minmagnitude") minMag: Double? = null,
        @Query("maxmagnitude") maxMag: Double? = null,

        // sıralama + kayıt sayısı
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 5000,

        // sadece deprem olayları
        @Query("eventtype") eventType: String = "earthquake"
    ): USGSResponseDto
}