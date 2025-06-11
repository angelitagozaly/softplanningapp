package com.example.softplanningapp.ui.addnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.softplanningapp.R
import com.example.softplanningapp.SoftPlanningApplication
import com.example.softplanningapp.data.entities.NoteType
import com.example.softplanningapp.data.entities.Priority
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AddNoteFragment : Fragment() {

    private lateinit var addNoteViewModel: AddNoteViewModel

    // UI Elements
    private lateinit var titleInput: TextInputEditText
    private lateinit var contentInput: TextInputEditText
    private lateinit var titleLayout: TextInputLayout
    private lateinit var contentLayout: TextInputLayout
    private lateinit var noteTypeGroup: ChipGroup
    private lateinit var priorityGroup: ChipGroup
    private lateinit var locationSpinner: Spinner
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = AddNoteViewModelFactory(application.repository)
        addNoteViewModel = ViewModelProvider(this, viewModelFactory)[AddNoteViewModel::class.java]

        initializeViews(view)
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        titleInput = view.findViewById(R.id.input_note_title)
        contentInput = view.findViewById(R.id.input_note_content)
        titleLayout = view.findViewById(R.id.layout_note_title)
        contentLayout = view.findViewById(R.id.layout_note_content)
        noteTypeGroup = view.findViewById(R.id.chip_group_note_type)
        priorityGroup = view.findViewById(R.id.chip_group_priority)
        locationSpinner = view.findViewById(R.id.spinner_location)
        saveButton = view.findViewById(R.id.button_save_note)
        cancelButton = view.findViewById(R.id.button_cancel)

        // Set default selections
        noteTypeGroup.check(R.id.chip_activity) // Default to Activity
        priorityGroup.check(R.id.chip_priority_medium) // Default to Medium priority
    }

    private fun setupObservers() {
        // Observe locations for spinner
        addNoteViewModel.allLocations.observe(viewLifecycleOwner) { locations ->
            val locationNames = mutableListOf("No location")
            locationNames.addAll(locations.map { it.name })

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                locationNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationSpinner.adapter = adapter
        }

        // Observe save result
        addNoteViewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                // Show success message
                Toast.makeText(context, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                // Navigate to notes list instead of home
                findNavController().navigate(R.id.action_addNote_to_notes)
            } else {
                Toast.makeText(context, "Error saving note", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe validation errors
        addNoteViewModel.titleError.observe(viewLifecycleOwner) { error ->
            titleLayout.error = error
        }

        addNoteViewModel.contentError.observe(viewLifecycleOwner) { error ->
            contentLayout.error = error
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveNote()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveNote() {
        val title = titleInput.text.toString().trim()
        val content = contentInput.text.toString().trim()

        // Get selected note type
        val noteType = when (noteTypeGroup.checkedChipId) {
            R.id.chip_activity -> NoteType.ACTIVITY
            R.id.chip_idea -> NoteType.IDEA_DUMP
            else -> NoteType.ACTIVITY
        }

        // Get selected priority
        val priority = when (priorityGroup.checkedChipId) {
            R.id.chip_priority_low -> Priority.LOW
            R.id.chip_priority_medium -> Priority.MEDIUM
            R.id.chip_priority_high -> Priority.HIGH
            else -> Priority.MEDIUM
        }

        // Get selected location (null if "No location" is selected)
        val selectedLocationId = if (locationSpinner.selectedItemPosition == 0) {
            null
        } else {
            addNoteViewModel.allLocations.value?.get(locationSpinner.selectedItemPosition - 1)?.id
        }

        // Save the note
        addNoteViewModel.saveNote(
            title = title,
            content = content,
            noteType = noteType,
            priority = priority,
            locationId = selectedLocationId
        )
    }
}