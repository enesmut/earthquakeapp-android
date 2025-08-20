package com.enesmut.earthquake

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.enesmut.earthquake.domain.Earthquake
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*

@Composable
fun EarthquakeMap(quakes: List<Earthquake>) {
    // Türkiye merkezine yakın başlangıç
    val turkey = LatLng(39.0, 35.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(turkey, 5f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        quakes.forEach { q ->
            val lat = q.latitude ?: return@forEach
            val lon = q.longitude ?: return@forEach
            val mag = q.magnitude ?: 0.0

            Marker(
                state = MarkerState(LatLng(lat, lon)),
                title = "Mw ${String.format("%.1f", mag)}",
                snippet = q.place ?: "Bilinmiyor",
                icon = BitmapDescriptorFactory.defaultMarker(
                    when {
                        mag >= 6.0 -> BitmapDescriptorFactory.HUE_RED
                        mag >= 4.0 -> BitmapDescriptorFactory.HUE_ORANGE
                        mag >= 2.0 -> BitmapDescriptorFactory.HUE_YELLOW
                        else -> BitmapDescriptorFactory.HUE_GREEN
                    }
                )
            )
        }
    }
}