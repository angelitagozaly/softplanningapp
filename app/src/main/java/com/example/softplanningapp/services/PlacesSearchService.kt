package com.example.softplanningapp.services

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.*
import com.example.softplanningapp.data.entities.PlaceSearchResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesSearchService(context: Context) {

    private val placesClient: PlacesClient

    init {
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyDCn4UQKzRKHtuf2xBp4U5EHaoqj91Fkh8")
        }
        placesClient = Places.createClient(context)
    }

    suspend fun searchPlaces(
        query: String,
        location: com.google.android.gms.maps.model.LatLng,
        radiusMeters: Int = 5000
    ): List<PlaceSearchResult> = suspendCancellableCoroutine { continuation ->

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES,
            Place.Field.RATING,
            Place.Field.PRICE_LEVEL
        )

        val circle = CircularBounds.newInstance(location, radiusMeters.toDouble())
        val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
            .setLocationBias(circle)
            .setMaxResultCount(10)
            .build()

        placesClient.searchByText(searchByTextRequest)
            .addOnSuccessListener { response ->
                val places = response.places.map { place ->
                    PlaceSearchResult(
                        placeId = place.id ?: "",
                        name = place.name ?: "",
                        address = place.address ?: "",
                        latitude = place.latLng?.latitude ?: 0.0,
                        longitude = place.latLng?.longitude ?: 0.0,
                        types = place.types?.map { it.name } ?: emptyList(),
                        rating = place.rating?.toFloat(),
                        priceLevel = place.priceLevel
                    )
                }
                continuation.resume(places)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    suspend fun searchNearbyPlaces(
        placeType: String,
        location: com.google.android.gms.maps.model.LatLng,
        radiusMeters: Int = 2000
    ): List<PlaceSearchResult> = suspendCancellableCoroutine { continuation ->

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES,
            Place.Field.RATING
        )

        val circle = CircularBounds.newInstance(location, radiusMeters.toDouble())
        val searchByTextRequest = SearchByTextRequest.builder(placeType, placeFields)
            .setLocationBias(circle)
            .setMaxResultCount(15)
            .build()

        placesClient.searchByText(searchByTextRequest)
            .addOnSuccessListener { response ->
                val places = response.places.map { place ->
                    PlaceSearchResult(
                        placeId = place.id ?: "",
                        name = place.name ?: "",
                        address = place.address ?: "",
                        latitude = place.latLng?.latitude ?: 0.0,
                        longitude = place.latLng?.longitude ?: 0.0,
                        types = place.types?.map { it.name } ?: emptyList(),
                        rating = place.rating?.toFloat()
                    )
                }
                continuation.resume(places)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}