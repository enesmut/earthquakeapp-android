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
        @Query("limit") limit: Int = 200
    ): USGSResponseDto
}