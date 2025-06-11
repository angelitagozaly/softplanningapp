package com.example.softplanningapp.ui.addlocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.softplanningapp.data.entities.Location
import com.example.softplanningapp.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Date

class AddLocationViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _operationResult = MutableLiveData<OperationResult>()
    val operationResult: LiveData<OperationResult> = _operationResult

    sealed class OperationResult {
        data class Success(val message: String) : OperationResult()
        data class Error(val message: String) : OperationResult()
    }

    fun saveLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Int
    ) {
        viewModelScope.launch {
            try {
                val location = Location(
                    name = name,
                    address = "", // We'll add address lookup later
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                    isActive = true,
                    createdAt = Date()
                )

                repository.insertLocation(location)
                _operationResult.value = OperationResult.Success("Location saved successfully!")

            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error("Failed to save location: ${e.message}")
            }
        }
    }
}

class AddLocationViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddLocationViewModel::class.java)) {
            return AddLocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}