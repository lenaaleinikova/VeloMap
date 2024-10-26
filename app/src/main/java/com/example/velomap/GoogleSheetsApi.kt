package com.example.velomap

import com.google.api.services.sheets.v4.model.ValueRange
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleSheetsApi {
    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getSheetData(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): Response<SheetResponse>

    @PUT("v4/spreadsheets/{spreadsheetId}/values/{range}?valueInputOption=RAW")
    suspend fun updateSheetValue(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Body body: ValueRange,
        @Query("key") apiKey: String
    ): Response<Void>
}
