package com.example.softplanningapp.ui.notes

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
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.ui.adapters.NotesAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment() {

    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notesAdapter: NotesAdapter

    // UI Elements
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipOngoing: Chip
    private lateinit var chipActivity: Chip
    private lateinit var chipIdea: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = NotesViewModelFactory(application.repository)
        notesViewModel = ViewModelProvider(this, viewModelFactory)[NotesViewModel::class.java]

        initializeViews(view)
        setupRecyclerView()
        setupObservers()
        setupSearchAndFilter()
    }

    private fun initializeViews(view: View) {
        searchView = view.findViewById(R.id.search_view)
        chipGroup = view.findViewById(R.id.chip_group_filter)
        chipAll = view.findViewById(R.id.chip_all)
        chipOngoing = view.findViewById(R.id.chip_ongoing)
        chipActivity = view.findViewById(R.id.chip_activity)
        chipIdea = view.findViewById(R.id.chip_idea)
        chipCompleted = view.findViewById(R.id.chip_completed)
        recyclerView = view.findViewById(R.id.recycler_notes)
        emptyTextView = view.findViewById(R.id.text_empty_notes)
        fab = view.findViewById(R.id.fab_add_note)

        // Set default filter
        chipAll.isChecked = true

        // FAB click listener
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_notes_to_addNote)
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onNoteClick = { note ->
                val action = NotesFragmentDirections.actionNotesToEditNote(note.id)
                findNavController().navigate(action)
            },
            onNoteComplete = { note ->
                notesViewModel.toggleNoteCompletion(note)
            },
            onNoteDelete = { note ->
                notesViewModel.deleteNote(note)
            }
        )

        recyclerView.apply {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        // Observe filtered notes
        notesViewModel.filteredNotes.observe(viewLifecycleOwner) { notes ->
            notesAdapter.submitList(notes)
            updateEmptyState(notes.isEmpty())
        }

        // Observe note count for each type
        notesViewModel.allNotes.observe(viewLifecycleOwner) { allNotes ->
            updateChipCounts(allNotes)
        }
    }

    private fun setupSearchAndFilter() {
        // Search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                notesViewModel.searchNotes(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                notesViewModel.searchNotes(newText ?: "")
                return true
            }
        })

        // Filter chips
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chip_all) -> {
                    notesViewModel.filterByType(FilterType.ALL)
                }
                checkedIds.contains(R.id.chip_ongoing) -> {
                    notesViewModel.filterByType(FilterType.ONGOING)
                }
                checkedIds.contains(R.id.chip_activity) -> {
                    notesViewModel.filterByType(FilterType.ACTIVITY)
                }
                checkedIds.contains(R.id.chip_idea) -> {
                    notesViewModel.filterByType(FilterType.IDEA)
                }
                checkedIds.contains(R.id.chip_completed) -> {
                    notesViewModel.filterByType(FilterType.COMPLETED)
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            // Update empty message based on search/filter state
            val currentQuery = searchView.query.toString()
            emptyTextView.text = if (currentQuery.isNotBlank()) {
                "No notes found for \"$currentQuery\""
            } else {
                "No notes yet.\nTap the + button to create your first note!"
            }
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateChipCounts(allNotes: List<Note>) {
        val ongoingCount = allNotes.count { !it.isCompleted }
        val activityCount = allNotes.count { it.noteType == com.example.softplanningapp.data.entities.NoteType.ACTIVITY && !it.isCompleted }
        val ideaCount = allNotes.count { it.noteType == com.example.softplanningapp.data.entities.NoteType.IDEA_DUMP && !it.isCompleted }
        val completedCount = allNotes.count { it.isCompleted }

        chipAll.text = "All (${allNotes.size})"
        chipOngoing.text = "🔄 Ongoing ($ongoingCount)"
        chipActivity.text = "🎯 Activity ($activityCount)"
        chipIdea.text = "💡 Ideas ($ideaCount)"
        chipCompleted.text = "✅ Completed ($completedCount)"
    }
}