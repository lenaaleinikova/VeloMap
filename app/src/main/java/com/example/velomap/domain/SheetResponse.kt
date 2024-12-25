package com.example.velomap.domain

data class SheetResponse(
    val range: String,
    val majorDimension: String,
    val values: List<List<String>>
)
