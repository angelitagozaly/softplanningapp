package com.example.softplanningapp.data.entities

data class PlaceSearchResult(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val types: List<String> = emptyList(),
    val rating: Float? = null,
    val priceLevel: Int? = null
)