package com.example.velomap.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.velomap.data.repository.PolygonRepository
import com.example.velomap.data.PolygonInfo
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PolygonRepository) : ViewModel() {

    private val _statuses = MutableLiveData<Result<List<Pair<String, String>>>>()
    val statuses: LiveData<Result<List<Pair<String, String>>>> get() = _statuses

    private val _polygonInfo = MutableLiveData<Result<List<PolygonInfo>>>()
    val polygonInfo: LiveData<Result<List<PolygonInfo>>> get() = _polygonInfo

    private val _accountName = MutableLiveData<String>()
    val accountName: LiveData<String> get() = _accountName



    fun setAccountName(accountName: String) {
        _accountName.value = accountName
    }

    fun fetchStatuses() {
        viewModelScope.launch {
            try {
                val result = repository.getStatuses()
                _statuses.postValue(Result.success(result))
            } catch (e: Exception) {
                _statuses.postValue(Result.failure(e))
            }
        }
    }

    fun fetchPolygonInfo() {
        viewModelScope.launch {
            try {
                val result = repository.getPolygonInfo()
                _polygonInfo.postValue(Result.success(result))
            } catch (e: Exception) {
                _polygonInfo.postValue(Result.failure(e))
            }
        }
    }
}

