package com.enesmut.earthquake.data
// JSON'dan okuyacağımız model:
// location: [latitude, longitude]
data class Province(
    val text: String,
    val keyNo: Int,
    val location: List<Double>
) {
    val lat: Double? get() = location.getOrNull(0)
    val lon: Double? get() = location.getOrNull(1)
}