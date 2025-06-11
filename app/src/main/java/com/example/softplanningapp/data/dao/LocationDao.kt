package com.example.softplanningapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.softplanningapp.data.entities.Location

@Dao
interface LocationDao {

    // Get all active locations, sorted by name
    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveLocations(): LiveData<List<Location>>

    // Get all locations (including inactive)
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): LiveData<List<Location>>

    // Check if location exists
    @Query("SELECT COUNT(*) FROM locations WHERE id = :id")
    suspend fun locationExists(id: Long): Int

    // Get a specific location by ID (non-nullable version)
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationByIdDirect(id: Long): Location

    // Search locations by name or address
    @Query("SELECT * FROM locations WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchLocations(query: String): LiveData<List<Location>>

    // Get locations within a certain distance (for geofence setup)
    @Query("SELECT * FROM locations WHERE isActive = 1")
    suspend fun getActiveLocationsForGeofencing(): List<Location>

    // Insert new location
    @Insert
    suspend fun insertLocation(location: Location): Long

    // Update existing location
    @Update
    suspend fun updateLocation(location: Location)

    // Delete location
    @Delete
    suspend fun deleteLocation(location: Location)

    // Deactivate location (soft delete) - with KSP this should work
    @Query("UPDATE locations SET isActive = 0 WHERE id = :locationId")
    suspend fun deactivateLocation(locationId: Long): Int

    // Reactivate location
    @Query("UPDATE locations SET isActive = 1 WHERE id = :locationId")
    suspend fun reactivateLocation(locationId: Long): Int
}