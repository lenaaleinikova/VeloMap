package com.example.velomap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _statuses = MutableLiveData<List<Pair<String, String>>>()
    val statuses: LiveData<List<Pair<String, String>>> get() = _statuses

    // Метод для загрузки статусов из Google Sheets
    suspend fun fetchStatuses(googleSheetsService: GoogleSheetsService) {
        val result = withContext(Dispatchers.IO) {
            googleSheetsService.fetchStatuses()
        }
        _statuses.value = result
    }
}
