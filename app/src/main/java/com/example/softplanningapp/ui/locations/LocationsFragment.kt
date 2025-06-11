package com.example.softplanningapp.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softplanningapp.R
import com.example.softplanningapp.SoftPlanningApplication
import com.example.softplanningapp.data.entities.Location
import com.example.softplanningapp.ui.adapters.LocationsAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LocationsFragment : Fragment() {

    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var locationsAdapter: LocationsAdapter

    // UI Elements
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = LocationsViewModelFactory(application.repository)
        locationsViewModel = ViewModelProvider(this, viewModelFactory)[LocationsViewModel::class.java]

        initializeViews(view)
        setupRecyclerView()
        setupObservers()
        setupSearch()
    }

    private fun initializeViews(view: View) {
        searchView = view.findViewById(R.id.search_view_locations)
        recyclerView = view.findViewById(R.id.recycler_locations)
        emptyTextView = view.findViewById(R.id.text_empty_locations)
        fab = view.findViewById(R.id.fab_add_location)

        // FAB click listener
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_locations_to_addLocation)
        }
    }

    private fun setupRecyclerView() {
        locationsAdapter = LocationsAdapter(
            onLocationClick = { location ->
                // TODO: Navigate to edit location when implemented
                // For now, just show a toast or navigate to add location with edit mode
            },
            onLocationToggle = { location ->
                locationsViewModel.toggleLocationActive(location)
            },
            onLocationDelete = { location ->
                locationsViewModel.deleteLocation(location)
            }
        )

        recyclerView.apply {
            adapter = locationsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        // Observe filtered locations
        locationsViewModel.filteredLocations.observe(viewLifecycleOwner) { locations ->
            locationsAdapter.submitList(locations)
            updateEmptyState(locations.isEmpty())
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                locationsViewModel.searchLocations(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                locationsViewModel.searchLocations(newText ?: "")
                return true
            }
        })
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            val currentQuery = searchView.query.toString()
            emptyTextView.text = if (currentQuery.isNotBlank()) {
                "No locations found for \"$currentQuery\""
            } else {
                "No locations saved yet.\nTap the + button to add your first location!"
            }
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}