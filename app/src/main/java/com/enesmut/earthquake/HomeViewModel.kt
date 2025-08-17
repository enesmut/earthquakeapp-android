package com.enesmut.earthquake

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enesmut.earthquake.data.EarthquakeRepository
import com.enesmut.earthquake.di.NetworkModule
import com.enesmut.earthquake.domain.Earthquake
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    // --- UI seçimleri ---
    var timeIndex by mutableStateOf(0) // 0:24,1:48,2:72,3:96
        private set
    val timeLabel: String
        get() = listOf("24 saat", "48 saat", "72 saat", "96 saat")[timeIndex]
    fun selectTime(i: Int) { timeIndex = i }

    var tabIndex by mutableStateOf(0) // 0: Liste, 1: Harita
        private set
    fun selectTab(i: Int) { tabIndex = i }

    var magSelection by mutableStateOf(setOf<Int>()) // 0:<2,1:2-<4,2:4-<6,3:≥6
        private set
    fun toggleMagnitude(i: Int) {
        magSelection = if (i in magSelection) magSelection - i else magSelection + i
    }

    // --- Data ---
    private val repo = EarthquakeRepository(NetworkModule.usgsService)
    var isLoading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var quakes by mutableStateOf<List<Earthquake>>(emptyList()); private set

    fun load() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val hours = listOf(24, 48, 72, 96)[timeIndex]
                val ranges: List<Pair<Double, Double>> =
                    if (magSelection.isEmpty()) emptyList()
                    else magSelection.sorted().map {
                        when (it) {
                            0 -> 0.0 to 2.0
                            1 -> 2.0 to 4.0
                            2 -> 4.0 to 6.0
                            3 -> 6.0 to 10.0 // üstü açık kabul; 10 yeterli
                            else -> 0.0 to 10.0
                        }
                    }
                quakes = repo.fetchEarthquakes(hours, ranges)
            } catch (t: Throwable) {
                error = t.message ?: "Bilinmeyen hata"
            } finally {
                isLoading = false
            }
        }
    }
}
