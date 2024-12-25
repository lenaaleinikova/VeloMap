package com.example.velomap.network

import android.content.Context
import android.util.Log
import com.example.velomap.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.FileInputStream
import java.io.InputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class GeoJsonLoader(
//    private val context: Context,
    val customTrustManager: CustomTrustManager,
    private val sslContext: SSLContext,
    private val trustManager: X509TrustManager
) {

    suspend fun fetchGeoJsonFromUrl(url: String): String? = withContext(Dispatchers.IO) {

//        val certInputStream: InputStream = context.resources.openRawResource(R.raw.helgilab)
//        val sslContext = customTrustManager.createSslContextWithCustomTrustManager(certInputStream)

        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                sslContext.socketFactory,
//                customTrustManager.getCustomTrustManager(certInputStream),  openssl s_client -connect geoserver.helgilab.ru:443 -CAfile C:/Users/elenaa/IdeaProjects/kotlin/VeloMap/app/src/main/res/raw/helgilab4.pem
                trustManager
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        return@withContext try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() // Возвращаем строку с данными JSON
            } else {
                Log.e("GeoJson", "Failed to fetch GeoJSON data: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("GeoJson", "Error fetching GeoJSON data", e)
            null
        }
    }
}