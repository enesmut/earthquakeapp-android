package com.enesmut.earthquake.data
import com.enesmut.earthquake.data.FeatureDto
import com.enesmut.earthquake.domain.Earthquake
// API FeatureDto -> Domain Earthquake dönüşümü
fun FeatureDto.toDomain(): Earthquake {
    val lon = geometry?.coordinates?.getOrNull(0)
    val lat = geometry?.coordinates?.getOrNull(1)
    val depth = geometry?.coordinates?.getOrNull(2)
    return Earthquake(
        id = id,
        place = properties.place,
        magnitude = properties.mag,
        timeMillis = properties.time,
        latitude = lat,
        longitude = lon,
        depthKm = depth,
        url = properties.url
    )
}