package com.example.softplanningapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // "Home", "Grocery Store", "Office"
    val address: String,                 // "123 Main St, City"
    val latitude: Double,                // GPS coordinates
    val longitude: Double,
    val radius: Int = 200,               // Geofence radius in meters
    val isActive: Boolean = true,        // Can disable locations
    val createdAt: Date
)