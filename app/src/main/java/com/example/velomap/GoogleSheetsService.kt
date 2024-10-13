package com.example.velomap

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
            response.body()?.values?.map { it[0] to it[1] } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
