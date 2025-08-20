package com.enesmut.earthquake.data


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// appContext.dataStore
private val Context.dataStore by preferencesDataStore(name = "settings")

private object SettingsKeys {
    val province = stringPreferencesKey("province")                   // "İstanbul"
    val notificationsEnabled = booleanPreferencesKey("notif_enabled") // false
    val notifMin = intPreferencesKey("notif_min")                     // 1..9
    val notifMax = intPreferencesKey("notif_max")                     // 1..9
}

class SettingsStore(private val context: Context) {

    // ---- Reads (Flows)
    val provinceFlow: Flow<String> =
        context.dataStore.data.map { it[SettingsKeys.province] ?: "İstanbul" }

    val notifEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { it[SettingsKeys.notificationsEnabled] ?: false }

    val notifMinFlow: Flow<Int> =
        context.dataStore.data.map { it[SettingsKeys.notifMin] ?: 1 }

    val notifMaxFlow: Flow<Int> =
        context.dataStore.data.map { it[SettingsKeys.notifMax] ?: 9 }

    // ---- Writes
    suspend fun setProvince(p: String) {
        context.dataStore.edit { it[SettingsKeys.province] = p }
    }

    suspend fun setNotifEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.notificationsEnabled] = enabled }
    }

    suspend fun setNotifMin(v: Int) {
        context.dataStore.edit { it[SettingsKeys.notifMin] = v.coerceIn(1, 9) }
    }

    suspend fun setNotifMax(v: Int) {
        context.dataStore.edit { it[SettingsKeys.notifMax] = v.coerceIn(1, 9) }
    }

    // Tek çağrıda aralık güncelle
    suspend fun setNotifRange(min: Int, max: Int) {
        val nMin = min.coerceIn(1, 9)
        val nMax = max.coerceIn(nMin, 9) // min ≤ max garantisi
        context.dataStore.edit {
            it[SettingsKeys.notifMin] = nMin
            it[SettingsKeys.notifMax] = nMax
        }
    }
}