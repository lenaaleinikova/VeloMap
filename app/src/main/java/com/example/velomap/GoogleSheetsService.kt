package com.example.velomap

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.api.services.sheets.v4.model.ValueRange


class GoogleSheetsService(private val apiKey: String) {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://sheets.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()



    private val googleSheetsApi: GoogleSheetsApi = retrofit.create(GoogleSheetsApi::class.java)

    suspend fun fetchStatuses(): List<Pair<String, String>> {
        val response = googleSheetsApi.getSheetData(
            "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18",
            "Вело-опер 2024 III часть!D2:I", // Диапазон ячеек со столбцами "16_id3" и "1_Статусы / 1=снято_"
            apiKey
        )
         Log.d("GeoJson", "response"+response.toString())

        return if (response.isSuccessful) {
            response.body()?.values?.map { it[0] to it[5] } ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun fetchInfo(): List<PolygonInfo> {
        val response = googleSheetsApi.getSheetData(
            "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18",
            "Вело-опер 2024 III часть!D2:K", // Диапазон ячеек со всеми необходимыми полями
            apiKey
        )
        Log.d("GeoJson", "response: ${response.toString()}")

        return if (response.isSuccessful) {
            response.body()?.values?.map {
                PolygonInfo(
                    id = it[0],
                    status = it[5],
                    operator = it[7]
                )
            } ?: emptyList()
        } else {
            emptyList()
        }
    }
    suspend fun updateStatus(polygonId: String, newStatus: String) {
        val spreadsheetId = "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18"
        val range = "Вело-опер 2024 III часть!D2:I" // настрой в зависимости от структуры таблицы

        val polygons = fetchInfo() // используй вспомогательную функцию для получения данных

        // 2. Находим индекс строки для polygonId
        val rowIndex = polygons.indexOfFirst { it.id == polygonId }
        Log.d("GeoJson", rowIndex.toString())
        if (rowIndex == -1) throw IllegalArgumentException("Полигон не найден в таблице")

        // 3. Отправляем запрос на обновление конкретной ячейки, учитывая строку
        val cellRange = "Вело-опер 2024 III часть!I$rowIndex" // предположим, что статус находится в колонке C
        val body = ValueRange().setValues(listOf(listOf(newStatus)))
//        googleSheetsApi.updateSheetValue(spreadsheetId, cellRange, body, apiKey)


    }

}
