package com.enesmut.earthquake


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var timeIndex by mutableStateOf(0)
        private set
    fun selectTime(i: Int) { timeIndex = i }
    var tabIndex by mutableStateOf(0)
        private set
    fun selectTab(i: Int) { tabIndex = i }
    val timeLabel: String
        get() = listOf("24 saat", "48 saat", "72 saat", "96 saat")[timeIndex]

    // Büyüklük seçimleri (çoklu seçim serbest)
    var magSelection by mutableStateOf(setOf<Int>())
        private set
    fun toggleMagnitude(i: Int) {
        magSelection = if (i in magSelection) magSelection - i else magSelection + i
    }
}
