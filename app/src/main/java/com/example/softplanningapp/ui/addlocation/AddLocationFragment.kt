package com.example.softplanningapp.ui.addlocation

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softplanningapp.R
import com.example.softplanningapp.SoftPlanningApplication
import com.example.softplanningapp.data.entities.PlaceSearchResult
import com.example.softplanningapp.services.PlacesSearchService
import com.example.softplanningapp.ui.adapters.PlaceSearchAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions

class AddLocationFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var addLocationViewModel: AddLocationViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // UI Elements
    private lateinit var textInputName: TextInputLayout
    private lateinit var editLocationName: TextInputEditText
    private lateinit var textInputSearch: TextInputLayout
    private lateinit var editSearchPlaces: TextInputEditText
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var recyclerSearchResults: RecyclerView
    private lateinit var radiusSlider: Slider
    private lateinit var buttonSave: MaterialButton
    private lateinit var buttonCancel: MaterialButton
    private lateinit var buttonCurrentLocation: MaterialButton
    private lateinit var buttonToggleCategories: MaterialButton
    private lateinit var cardSearchResults: MaterialCardView
    private lateinit var textRadiusValue: TextView

    // Search functionality
    private lateinit var placeSearchAdapter: PlaceSearchAdapter
    private lateinit var placesSearchService: PlacesSearchService

    // Location variables
    private var selectedLatLng: LatLng? = null
    private val defaultLocation = LatLng(37.4419, -122.1430) // Default to Palo Alto

    companion object {
        private const val TAG = "AddLocationFragment"
        private const val LOCATION_PERMISSION_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated called")

        // Check Google Play Services first
        if (!checkGooglePlayServices()) {
            Toast.makeText(context, "Google Play Services not available", Toast.LENGTH_LONG).show()
            return
        }

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = AddLocationViewModelFactory(application.repository)
        addLocationViewModel = ViewModelProvider(this, viewModelFactory)[AddLocationViewModel::class.java]

        initializeViews(view)
        setupMap()
        setupObservers()
        setupClickListeners()
        setupSearchFunctionality()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initializeViews(view: View) {
        textInputName = view.findViewById(R.id.text_input_location_name)
        editLocationName = view.findViewById(R.id.edit_location_name)
        textInputSearch = view.findViewById(R.id.text_input_search)
        editSearchPlaces = view.findViewById(R.id.edit_search_places)
        chipGroupCategories = view.findViewById(R.id.chip_group_categories)
        recyclerSearchResults = view.findViewById(R.id.recycler_search_results)
        radiusSlider = view.findViewById(R.id.radius_slider)
        buttonSave = view.findViewById(R.id.button_save_location)
        buttonCancel = view.findViewById(R.id.button_cancel)
        buttonCurrentLocation = view.findViewById(R.id.button_current_location)
        buttonToggleCategories = view.findViewById(R.id.button_toggle_categories)
        cardSearchResults = view.findViewById(R.id.card_search_results)
        textRadiusValue = view.findViewById(R.id.text_radius_value)

        // Initialize search service
        placesSearchService = PlacesSearchService(requireContext())

        // Setup search results adapter
        placeSearchAdapter = PlaceSearchAdapter { place ->
            selectPlaceFromSearch(place)
        }
        recyclerSearchResults.apply {
            adapter = placeSearchAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Setup radius slider listener
        radiusSlider.addOnChangeListener { _, value, _ ->
            textRadiusValue.text = "${value.toInt()}m"
        }

        // Initially hide categories and search results
        chipGroupCategories.visibility = View.GONE
        cardSearchResults.visibility = View.GONE
    }

    private fun setupMap() {
        Log.d(TAG, "Setting up map fragment")
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

        if (mapFragment != null) {
            Log.d(TAG, "Map fragment found, calling getMapAsync")
            mapFragment.getMapAsync(this)
        } else {
            Log.e(TAG, "Map fragment is null! Check your layout")
            Toast.makeText(context, "Map fragment not found", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        addLocationViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddLocationViewModel.OperationResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    // Navigate to locations list regardless of where we came from
                    findNavController().navigate(R.id.action_addLocation_to_locations)
                }
                is AddLocationViewModel.OperationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        buttonSave.setOnClickListener {
            if (validateInput()) {
                saveLocation()
            }
        }

        buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        buttonCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        // Toggle categories visibility
        buttonToggleCategories.setOnClickListener {
            toggleCategoriesVisibility()
        }
    }

    private fun setupSearchFunctionality() {
        // Text search with Enter key
        editSearchPlaces.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val query = editSearchPlaces.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        // Clear search results when search text is cleared
        editSearchPlaces.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editSearchPlaces.text.toString().trim().isEmpty()) {
                hideSearchResults()
            }
        }

        // Category chips
        setupCategoryChips()
    }

    private fun setupCategoryChips() {
        // Set individual click listeners for each chip
        view?.findViewById<View>(R.id.chip_grocery)?.setOnClickListener {
            performCategorySearch("grocery store")
        }
        view?.findViewById<View>(R.id.chip_restaurant)?.setOnClickListener {
            performCategorySearch("restaurant")
        }
        view?.findViewById<View>(R.id.chip_gas_station)?.setOnClickListener {
            performCategorySearch("gas station")
        }
        view?.findViewById<View>(R.id.chip_pharmacy)?.setOnClickListener {
            performCategorySearch("pharmacy")
        }
        view?.findViewById<View>(R.id.chip_bank)?.setOnClickListener {
            performCategorySearch("bank")
        }
    }

    private fun toggleCategoriesVisibility() {
        if (chipGroupCategories.visibility == View.VISIBLE) {
            // Hide categories
            chipGroupCategories.visibility = View.GONE
            buttonToggleCategories.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_more_24)
        } else {
            // Show categories
            chipGroupCategories.visibility = View.VISIBLE
            buttonToggleCategories.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_less_24)
        }
    }

    private fun performSearch(query: String) {
        val searchLocation = selectedLatLng ?: googleMap.cameraPosition.target

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val results = placesSearchService.searchPlaces(query, searchLocation)
                showSearchResults(results)
            } catch (e: Exception) {
                Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                hideSearchResults()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun performCategorySearch(category: String) {
        val searchLocation = selectedLatLng ?: googleMap.cameraPosition.target

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                val results = placesSearchService.searchNearbyPlaces(category, searchLocation)
                showSearchResults(results)

                // Update search text to show what was searched
                editSearchPlaces.setText(category)
            } catch (e: Exception) {
                Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                hideSearchResults()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showSearchResults(results: List<PlaceSearchResult>) {
        if (results.isNotEmpty()) {
            placeSearchAdapter.submitList(results)
            cardSearchResults.visibility = View.VISIBLE

            // Add markers to map
            addSearchResultsToMap(results)

            Toast.makeText(context, "Found ${results.size} places", Toast.LENGTH_SHORT).show()
        } else {
            hideSearchResults()
            Toast.makeText(context, "No places found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSearchResults() {
        cardSearchResults.visibility = View.GONE
        // Clear search markers, keep only selected marker
        redrawMapMarkers()
    }

    private fun showLoading(show: Boolean) {
        buttonSave.isEnabled = !show
        buttonCurrentLocation.isEnabled = !show
        buttonToggleCategories.isEnabled = !show
    }

    private fun addSearchResultsToMap(results: List<PlaceSearchResult>) {
        // Clear all markers
        googleMap.clear()

        // Re-add selected marker if exists (red marker)
        selectedLatLng?.let {
            googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        // Add search result markers (blue markers)
        results.forEach { place ->
            val latLng = LatLng(place.latitude, place.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(place.name)
                    .snippet(place.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }

        // Adjust camera to show all results if there are any
        if (results.isNotEmpty()) {
            val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
            results.forEach { place ->
                bounds.include(LatLng(place.latitude, place.longitude))
            }
            selectedLatLng?.let { bounds.include(it) }

            try {
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
                googleMap.animateCamera(cameraUpdate)
            } catch (e: Exception) {
                // Fallback if bounds are invalid
                if (results.isNotEmpty()) {
                    val firstResult = results.first()
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(firstResult.latitude, firstResult.longitude), 14f
                        )
                    )
                }
            }
        }
    }

    private fun redrawMapMarkers() {
        googleMap.clear()
        selectedLatLng?.let { updateMapMarker(it) }
    }

    private fun selectPlaceFromSearch(place: PlaceSearchResult) {
        // Set the selected location
        val latLng = LatLng(place.latitude, place.longitude)
        selectedLatLng = latLng

        // Update map with red marker for selection
        updateMapMarker(latLng)

        // Pre-fill location name if it's empty
        if (editLocationName.text.toString().trim().isEmpty()) {
            editLocationName.setText(place.name)
        }

        // Hide search results and collapse categories
        hideSearchResults()
        chipGroupCategories.visibility = View.GONE
        buttonToggleCategories.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_more_24)

        // Clear search text
        editSearchPlaces.text?.clear()

        // Clear any previous errors
        textInputName.error = null

        Toast.makeText(context, "Selected: ${place.name}", Toast.LENGTH_SHORT).show()
    }

    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())

        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services is available")
                true
            }
            else -> {
                Log.e(TAG, "Google Play Services not available: $resultCode")
                if (googleApiAvailability.isUserResolvableError(resultCode)) {
                    googleApiAvailability.getErrorDialog(this, resultCode, 9000)?.show()
                }
                false
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "onMapReady called - map is ready!")
        googleMap = map

        // Set up map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        // Set click listener for map
        googleMap.setOnMapClickListener { latLng ->
            Log.d(TAG, "Map clicked at: $latLng")
            selectedLatLng = latLng
            updateMapMarker(latLng)

            // Hide search results when user manually selects location
            hideSearchResults()
            chipGroupCategories.visibility = View.GONE
            buttonToggleCategories.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_more_24)
        }

        // Move to default location
        Log.d(TAG, "Moving camera to default location: $defaultLocation")
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        // Check for location permission
        if (hasLocationPermission()) {
            Log.d(TAG, "Location permission granted")
            enableMyLocation()
            getCurrentLocation()
        } else {
            Log.d(TAG, "Location permission not granted, requesting...")
            requestLocationPermission()
        }

        Toast.makeText(context, "Map loaded successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun updateMapMarker(latLng: LatLng) {
        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), *REQUIRED_PERMISSIONS)
    }

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This app needs location permission to show your current location",
            LOCATION_PERMISSION_CODE,
            *REQUIRED_PERMISSIONS
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            googleMap.isMyLocationEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                selectedLatLng = currentLatLng
                updateMapMarker(currentLatLng)
                Toast.makeText(context, "Using current location", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(): Boolean {
        val name = editLocationName.text.toString().trim()

        textInputName.error = null
        var isValid = true

        if (name.isEmpty()) {
            textInputName.error = "Location name is required"
            isValid = false
        } else if (name.length < 2) {
            textInputName.error = "Name must be at least 2 characters"
            isValid = false
        }

        if (selectedLatLng == null) {
            Toast.makeText(context, "Please select a location on the map or from search results", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveLocation() {
        val name = editLocationName.text.toString().trim()
        val radius = radiusSlider.value.toInt()
        val latLng = selectedLatLng ?: return

        addLocationViewModel.saveLocation(
            name = name,
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            radius = radius
        )
    }

    // Permission callbacks
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION_PERMISSION_CODE && ::googleMap.isInitialized) {
            enableMyLocation()
            getCurrentLocation()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}