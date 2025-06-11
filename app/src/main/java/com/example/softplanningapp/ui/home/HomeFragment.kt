package com.example.softplanningapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softplanningapp.R
import com.example.softplanningapp.SoftPlanningApplication
import com.example.softplanningapp.ui.adapters.RecentNotesAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recentNotesAdapter: RecentNotesAdapter

    // UI Elements
    private lateinit var textTotalNotes: TextView
    private lateinit var recyclerRecentNotes: RecyclerView
    private lateinit var textEmptyState: TextView
    private lateinit var cardAddNote: MaterialCardView
    private lateinit var cardAddLocation: MaterialCardView
    private lateinit var buttonViewAllNotes: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = HomeViewModelFactory(application.repository)
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        initializeViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        textTotalNotes = view.findViewById(R.id.text_total_notes)
        recyclerRecentNotes = view.findViewById(R.id.recycler_recent_notes)
        textEmptyState = view.findViewById(R.id.text_no_notes)
        cardAddNote = view.findViewById(R.id.card_add_note)
        cardAddLocation = view.findViewById(R.id.card_add_location)
        buttonViewAllNotes = view.findViewById(R.id.button_view_all_notes)
    }

    private fun setupRecyclerView() {
        recentNotesAdapter = RecentNotesAdapter(
            onNoteClick = { note ->
                val action = HomeFragmentDirections.actionHomeToEditNote(note.id)
                findNavController().navigate(action)
            },
            onNoteComplete = { note ->
                homeViewModel.toggleNoteCompletion(note)
            },
            onNoteDelete = { note ->
                homeViewModel.deleteNote(note)
            }
        )

        recyclerRecentNotes.apply {
            adapter = recentNotesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        // Observe statistics
        homeViewModel.totalNotes.observe(viewLifecycleOwner) { total ->
            textTotalNotes.text = total.toString()
        }

        // Observe recent notes
        homeViewModel.recentNotes.observe(viewLifecycleOwner) { notes ->
            recentNotesAdapter.submitList(notes)
            updateEmptyState(notes.isEmpty())
        }
    }

    private fun setupClickListeners() {
        // Add Note Card
        cardAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addNote)
        }

        // Add Location Card - NEW!
        cardAddLocation.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addLocation)
        }

        // View All Notes Button
        buttonViewAllNotes.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_notes)
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            textEmptyState.visibility = View.VISIBLE
            recyclerRecentNotes.visibility = View.GONE
        } else {
            textEmptyState.visibility = View.GONE
            recyclerRecentNotes.visibility = View.VISIBLE
        }
    }
}