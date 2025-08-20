package com.enesmut.earthquake
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.enesmut.earthquake.data.EarthquakeRepository
import com.enesmut.earthquake.di.NetworkModule
import com.enesmut.earthquake.domain.Earthquake
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel : ViewModel() {

    // --- UI seçimleri ---
    var timeIndex by mutableStateOf(0)   // 0:24, 1:48, 2:72, 3:96
        private set
    val timeLabel: String
        get() = listOf("24 saat", "48 saat", "72 saat", "96 saat")[timeIndex]

    var tabIndex by mutableStateOf(0)    // 0: Liste, 1: Harita
        private set

    var magSelection by mutableStateOf(setOf<Int>()) // 0:<2, 1:2-<4, 2:4-<6, 3:≥6
        private set

    // --- Data ---
    private val repo =
        EarthquakeRepository(NetworkModule.usgsService) // adı usgsService değilse usgs olsun
    var isLoading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var quakes by mutableStateOf<List<Earthquake>>(emptyList()); private set

    private var loadJob: Job? = null

    init {
        load()  // açılışta otomatik veri çek
    }

    fun selectTime(i: Int) {
        timeIndex = i
        load()
    }

    fun selectTab(i: Int) {
        tabIndex = i
        // liste/harita geçişinde yükleme zorunlu değil; istersen burada da load() çağırabilirsin
    }

    fun toggleMagnitude(i: Int) {
        magSelection = if (i in magSelection) magSelection - i else magSelection + i
        load()
    }

    fun clearMagnitudes() {
        magSelection = emptySet()
        load()
    }

    fun load() {
        // önceki network çağrısını iptal et
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            isLoading = true
            // önceki hatayı temizle (ama iptalden doğanı göstermeyeceğiz)
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
                            3 -> 6.0 to 10.0
                            else -> 0.0 to 10.0
                        }
                    }

                quakes = repo.fetchTurkey(hoursWindow = hours, ranges = ranges)
            } catch (ce: CancellationException) {
                // iptal normal bir durum; hatayı göstermiyoruz
                // isteğe bağlı: logla ama UI'ya yansıtma
                // Log.d("VM", "load() cancelled")
            } catch (t: Throwable) {
                // gerçek bir hata
                error = t.message ?: "Bilinmeyen hata"
                quakes = emptyList()
            } finally {
                isLoading = false
            }
        }}}
