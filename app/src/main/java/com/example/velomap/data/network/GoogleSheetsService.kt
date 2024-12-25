package com.example.velomap.data.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.velomap.domen.PolygonInfo
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class GoogleSheetsService(private val apiKey: String, context: Context) {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://sheets.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val googleSheetsApi: GoogleSheetsApi = retrofit.create(GoogleSheetsApi::class.java)

    private fun getSheetsService(context: Context, accountName: String): Sheets {
        val credentials = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE)
        ).apply {
            selectedAccountName = accountName
        }

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credentials
        )
            .setApplicationName("VeloMap")
            .build()
    }

    suspend fun fetchStatuses(): List<Pair<String, String>> {
        val response = googleSheetsApi.getSheetData(
            "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18",
            "Вело-опер 2024 III часть!D2:I", // Диапазон ячеек со столбцами "16_id3" и "1_Статусы / 1=снято_"
            apiKey
        )
        Log.d("GeoJson", "response$response")
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
        googleSheetsApi.updateSheetValue(spreadsheetId, cellRange, body, apiKey)


    }

    suspend fun testUpdateCell(
        googleAccountCredential: GoogleAccountCredential,
        context: Context
    ): Intent? {

        try {
            withContext(Dispatchers.IO) {
                val spreadsheetId = "1GuzQu1G3MXVc9K9WQu3qXG2W6gys8XP6mkWgeMRGP18"
                val cellRange = "Вело-опер 2024 III часть!I25"
                val body = ValueRange().setValues(listOf(listOf("тестовое значение")))
                Log.d(
                    "testUpdate",
                    "Обновлено ${googleAccountCredential.selectedAccountName} ячеек."
                )
                val sheetsService =
                    getSheetsService(context, googleAccountCredential.selectedAccountName)
                Log.d("testUpdate", "Обновлено ${sheetsService} ячеек.")
                val updateRequest =
                    sheetsService.spreadsheets().values().update(spreadsheetId, cellRange, body)
                updateRequest.setValueInputOption("RAW")


                val response2 = updateRequest.execute()
                Log.d("testUpdate", "Обновлено ${response2} ячеек.")

            }
            return null
        } catch (e: UserRecoverableAuthException) {
            Log.e("testUpdate", "Ошибка обновления записи", e)
            return e.intent
        }
    }
}
