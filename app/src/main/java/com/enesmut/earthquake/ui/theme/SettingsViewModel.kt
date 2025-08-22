package com.enesmut.earthquake.ui.theme


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enesmut.earthquake.data.Province
import com.enesmut.earthquake.data.ProvincesRepository
import com.enesmut.earthquake.data.SettingsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app)

    // JSON'dan gelen 81 il
    private val _provinces = MutableStateFlow<List<Province>>(emptyList())
    val provinces: StateFlow<List<Province>> = _provinces.asStateFlow()

    init {
        _provinces.value = ProvincesRepository.loadFromAssets(getApplication())
    }

    data class UiState(
        val province: String = "İstanbul",
        val notificationsEnabled: Boolean = false,
        val notifMin: Int = 1,
        val notifMax: Int = 9
    )

    val state: StateFlow<UiState> = combine(
        store.provinceFlow,
        store.notifEnabledFlow,
        store.notifMinFlow,
        store.notifMaxFlow
    ) { p, enabled, minV, maxV ->
        val nMin = min(9, max(1, minV))
        val nMax = min(9, max(nMin, maxV)) // min ≤ max
        UiState(
            province = p,
            notificationsEnabled = enabled,
            notifMin = nMin,
            notifMax = nMax
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    fun setProvince(p: String) = viewModelScope.launch { store.setProvince(p) }
    fun setNotificationsEnabled(v: Boolean) = viewModelScope.launch { store.setNotifEnabled(v) }
    fun setNotifRange(minValue: Int, maxValue: Int) = viewModelScope.launch {
        val nMin = min(9, max(1, minValue))
        val nMax = min(9, max(nMin, maxValue))
        store.setNotifRange(nMin, nMax)
    }
    init {
        val list = ProvincesRepository.loadFromAssets(getApplication())
        _provinces.value = list
        if (list.isEmpty()) {
            // Yükleme başarısızsa bile UiState yine çalışır; province string'tir.
            // İstersen burada bir log da bas:
            android.util.Log.d("SettingsVM", "Il listesi yüklendi: ${list.size} kayıt")
            android.util.Log.w("SettingsVM", "Province listesi boş geldi; fallback devrede.")
        }
    }

    // (opsiyonel) seçili ismin Province karşılığını alırken boş listeye tolerans:
    fun selectedProvinceOrNull(name: String): Province? =
        provinces.value.firstOrNull { it.text.equals(name, ignoreCase = true) }
}