package com.enesmut.earthquake.data


import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ProvincesRepository {
    private const val TAG = "ProvincesRepo"
    private const val FILE_NAME = "provinces.json"

    fun loadFromAssets(context: Context): List<Province> {
        return try {
            val json = context.assets.open(FILE_NAME)
                .bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Province>>() {}.type
            Gson().fromJson<List<Province>>(json, type) ?: emptyList()
        } catch (t: Throwable) {
            Log.e(TAG, "Assets/$FILE_NAME okunamadı: ${t.message}")
            // ÇÖKME yerine küçük bir fallback list veriyoruz:
            listOf(
                Province("İstanbul", 34, listOf(41.0082, 28.9784)),
                Province("Ankara", 6, listOf(39.9334, 32.8597)),
                Province("İzmir", 35, listOf(38.4237, 27.1428))
            )
        }
    }
}