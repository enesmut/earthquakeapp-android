package com.enesmut.earthquake.data


data class USGSResponseDto(
    val type: String? = null,
    val features: List<FeatureDto> = emptyList()
)

data class FeatureDto(
    val id: String,
    val properties: PropertiesDto,
    val geometry: GeometryDto?
)

data class PropertiesDto(
    val mag: Double?,
    val place: String?,
    val time: Long?,          // epoch millis
    val url: String?
)

data class GeometryDto(
    val type: String?,
    val coordinates: List<Double>? // [lon, lat, depth_km]
)