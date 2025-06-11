package com.example.softplanningapp.ui.addlocation

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.softplanningapp.R
import com.example.softplanningapp.SoftPlanningApplication
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pub.devrel.easypermissions.EasyPermissions

class AddLocationFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var addLocationViewModel: AddLocationViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // UI Elements
    private lateinit var textInputName: TextInputLayout
    private lateinit var editLocationName: TextInputEditText
    private lateinit var radiusSlider: Slider
    private lateinit var buttonSave: MaterialButton
    private lateinit var buttonCancel: MaterialButton
    private lateinit var buttonCurrentLocation: MaterialButton

    // Location variables
    private var selectedLatLng: LatLng? = null
    private val defaultLocation = LatLng(37.4419, -122.1430) // Default to Palo Alto

    companion object {
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

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = AddLocationViewModelFactory(application.repository)
        addLocationViewModel = ViewModelProvider(this, viewModelFactory)[AddLocationViewModel::class.java]

        initializeViews(view)
        setupMap()
        setupObservers()
        setupClickListeners()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initializeViews(view: View) {
        textInputName = view.findViewById(R.id.text_input_location_name)
        editLocationName = view.findViewById(R.id.edit_location_name)
        radiusSlider = view.findViewById(R.id.radius_slider)
        buttonSave = view.findViewById(R.id.button_save_location)
        buttonCancel = view.findViewById(R.id.button_cancel)
        buttonCurrentLocation = view.findViewById(R.id.button_current_location)
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupObservers() {
        addLocationViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddLocationViewModel.OperationResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
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
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set up map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        // Set click listener for map
        googleMap.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            updateMapMarker(latLng)
        }

        // Move to default location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        // Check for location permission and get current location
        if (hasLocationPermission()) {
            enableMyLocation()
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun updateMapMarker(latLng: LatLng) {
        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
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
            googleMap.uiSettings.isMyLocationButtonEnabled = true
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
            } ?: run {
                Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
            }
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
            Toast.makeText(context, "Please select a location on the map", Toast.LENGTH_SHORT).show()
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