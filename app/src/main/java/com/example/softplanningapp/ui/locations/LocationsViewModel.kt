package com.example.softplanningapp.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.softplanningapp.data.entities.Location
import com.example.softplanningapp.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Date

class LocationsViewModel(private val repository: AppRepository) : ViewModel() {

    // All locations from database
    val allLocations: LiveData<List<Location>> = repository.getAllLocations()

    // Current search query
    private val _searchQuery = MutableLiveData("")
    private val searchQuery: LiveData<String> = _searchQuery

    // Filtered locations based on search
    val filteredLocations = MediatorLiveData<List<Location>>().apply {
        addSource(allLocations) { locations ->
            value = applyFilters(locations, searchQuery.value ?: "")
        }
        addSource(searchQuery) { query ->
            value = applyFilters(allLocations.value ?: emptyList(), query)
        }
    }

    fun searchLocations(query: String) {
        _searchQuery.value = query
    }

    fun toggleLocationActive(location: Location) {
        viewModelScope.launch {
            try {
                val updatedLocation = location.copy(
                    isActive = !location.isActive
                )
                repository.updateLocation(updatedLocation)
            } catch (e: Exception) {
                // Handle error - could show a toast or error state
            }
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            try {
                repository.deleteLocation(location)
            } catch (e: Exception) {
                // Handle error - could show a toast or error state
            }
        }
    }

    private fun applyFilters(locations: List<Location>, query: String): List<Location> {
        if (query.isBlank()) {
            return locations
        }

        return locations.filter { location ->
            location.name.contains(query, ignoreCase = true) ||
                    location.address.contains(query, ignoreCase = true)
        }
    }
}

class LocationsViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationsViewModel::class.java)) {
            return LocationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}