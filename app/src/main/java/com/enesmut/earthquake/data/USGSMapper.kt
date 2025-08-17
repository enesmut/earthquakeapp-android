package com.enesmut.earthquake.data
import com.enesmut.earthquake.data.FeatureDto
import com.enesmut.earthquake.domain.Earthquake

fun FeatureDto.toDomain(): Earthquake {
    val coords = geometry?.coordinates
    val lon = coords?.getOrNull(0)
    val lat = coords?.getOrNull(1)
    val depth = coords?.getOrNull(2)
    return Earthquake(
        id = id,
        magnitude = properties.mag,
        place = properties.place,
        timeMillis = properties.time,
        latitude = lat,
        longitude = lon,
        depthKm = depth,
        url = properties.url
    )
}