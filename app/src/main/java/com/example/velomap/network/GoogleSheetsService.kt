package com.example.velomap.network

import android.content.Context
import android.util.Log
import com.example.velomap.data.PolygonInfo
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GoogleSheetsService(private val apiKey: String, context: Context) {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://sheets.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val googleSheetsApi: GoogleSheetsApi = retrofit.create(GoogleSheetsApi::class.java)
    private val sheetsService: Sheets by lazy { getSheetsService(context) }

    private fun getSheetsService(context: Context): Sheets {
        val credentials = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(SheetsScopes.SPREADSHEETS)
        )

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credentials
        )
            .setApplicationName("Your App Name")
            .build()
    }

    suspend fun fetchStatuses(): List<Pair<String, String>> {
        val response = googleSheetsApi.getSheetData(
            "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18",
            "Вело-опер 2024 III часть!D2:I", // Диапазон ячеек со столбцами "16_id3" и "1_Статусы / 1=снято_"
            apiKey
        )
        Log.d("GeoJson", "response" + response.toString())

        return if (response.isSuccessful) {
            response.body()?.values?.map { it[0] to it[5] } ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun fetchInfo(): List<PolygonInfo> {
        val apiKeyHash = "AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE"
        val response = googleSheetsApi.getSheetData(
            "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18",
            "Вело-опер 2024 III часть!D2:K",
            apiKey
        )

        Log.d("GeoJson", "response: ${response.toString()}")
        Log.d("GeoJson", "response1: ${response.body()?.values}")

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
        val range = "Вело-опер 2024 III часть!D2:I"

        val polygons = fetchInfo()


        val rowIndex = polygons.indexOfFirst { it.id == polygonId }
        Log.d("testUpdate", rowIndex.toString())
        if (rowIndex == -1) throw IllegalArgumentException("Полигон не найден в таблице")


        val cellRange = "Вело-опер 2024 III часть!I${rowIndex + 2}"
        Log.d("testUpdate", cellRange)
        val body = ValueRange().setValues(listOf(listOf(newStatus)))
//        Log.d("PolygonInfoDialog", cellRange)
        googleSheetsApi.updateSheetValue(spreadsheetId, cellRange, body, apiKey)


    }

    suspend fun testUpdateCell() {
        val spreadsheetId = "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18"
        val cellRange = "Вело-опер 2024 III часть!I25"
        val body = ValueRange().setValues(listOf(listOf("тестовое значение")))

        try {
            withContext(Dispatchers.IO) {
//
//                val response =
//                    googleSheetsApi.updateSheetValue(spreadsheetId, cellRange, body, apiKey)
//                Log.d("testUpdate", "Ответ от API: $response")
//                if (response.isSuccessful) Log.d(
//                    "testUpdate",
//                    "Запись успешно обновлена."
//                ) else {
//                    Log.d("testUpdate", response.code().toString())
//                }


                val updateRequest =
                    sheetsService.spreadsheets().values().update(spreadsheetId, cellRange, body)
                updateRequest.setValueInputOption("RAW")
                val response2 = updateRequest.execute()
                Log.d("testUpdate", "Обновлено ${response2.updatedCells} ячеек.")
            }
        } catch (e: Exception) {
            Log.e("testUpdate", "Ошибка обновления записи", e)
        }
    }

    suspend fun chooseAccount(credential: GoogleAccountCredential) {

        val sheetsService: Sheets = Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Your App Name")
            .build()
    }


}
