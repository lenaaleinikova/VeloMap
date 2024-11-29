package com.example.velomap

import android.util.Log
import com.example.velomap.data.PolygonInfo
import com.example.velomap.network.GoogleSheetsService

class PolygonRepository(private val googleSheetsService: GoogleSheetsService) {

    suspend fun getStatuses(): List<Pair<String, String>> {
        return try {
            googleSheetsService.fetchStatuses()
        } catch (e: Exception) {
            // Логируем ошибку и передаем дальше
            Log.e("PolygonRepository", "Error fetching statuses", e)
            throw e
        }
    }

    suspend fun getPolygonInfo(): List<PolygonInfo> {
        return try {
            googleSheetsService.fetchInfo()
        } catch (e: Exception) {
            Log.e("PolygonRepository", "Error fetching polygon info", e)
            throw e
        }
    }
}

