package com.example.velomap.data.network

import android.content.Context
import android.util.Log
import com.example.velomap.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeoJsonManager(context: Context) {

    private val customTrustManager = CustomTrustManager()
    private val sslContext = context.resources.openRawResource(R.raw.helgilab).use {
        customTrustManager.createSslContextWithCustomTrustManager(it)
    }
    private val trustManager = context.resources.openRawResource(R.raw.helgilab).use {
        customTrustManager.getCustomTrustManager(it)
    }
    private val geoJsonLoader = GeoJsonLoader(customTrustManager, sslContext, trustManager)

    suspend fun fetchGeoJson(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                geoJsonLoader.fetchGeoJsonFromUrl(url)
            } catch (e: Exception) {
                Log.e("GeoJsonManager", "Error fetching GeoJSON", e)
                null
            }
        }
    }
}