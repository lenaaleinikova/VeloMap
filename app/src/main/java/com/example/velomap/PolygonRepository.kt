package com.example.velomap

import android.util.Log
import com.example.velomap.data.PolygonInfo
import com.example.velomap.network.GoogleSheetsService

class PolygonRepository(private val googleSheetsService: GoogleSheetsService) {

    suspend fun getStatuses(): List<Pair<String, String>> {
        return try {
            val data = googleSheetsService.fetchInfo()
            Log.d("Polygon", "Fetched data: $data")
            googleSheetsService.fetchStatuses()
        } catch (e: Exception) {
            Log.e("Polygon", "Error fetching statuses", e)
            throw e
        }
    }

    suspend fun getPolygonInfo(): List<PolygonInfo> {
        return try {
            val data = googleSheetsService.fetchInfo()
            Log.d("Polygon", "getPolygonInfo data: $data")
            googleSheetsService.fetchInfo()
        } catch (e: Exception) {
            Log.e("Polygon", "Error fetching polygon info", e)
            throw e
        }
    }
}

