package com.example.velomap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _statuses = MutableLiveData<List<Pair<String, String>>>()
    val statuses: LiveData<List<Pair<String, String>>> get() = _statuses

    private val _polygonInfo = MutableLiveData<List<PolygonInfo>>()
    val polygonInfo: LiveData<List<PolygonInfo>> get()= _polygonInfo

    private val _polygonInfoMap = mutableMapOf<String, PolygonInfo>()
    val polygonInfoMap: Map<String, PolygonInfo> get() = _polygonInfoMap

    suspend fun fetchStatuses(googleSheetsService: GoogleSheetsService) {
        val result = withContext(Dispatchers.IO) {
            googleSheetsService.fetchStatuses()
        }
        _statuses.value = result
    }
    suspend fun fetchPolygonInfo(googleSheetsService: GoogleSheetsService) {
        val result = withContext(Dispatchers.IO) {
            googleSheetsService.fetchInfo()
        }
        _polygonInfo.value = result
    }
//    fun getPolygonCoordinates(polygonId: String): Pair<Double, Double>? {
//        val polygonInfo = _polygonInfoMap[polygonId]
//        return polygonInfo?.let { Pair(it.longitude, it.latitude) }
//    }

}
