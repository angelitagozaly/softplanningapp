package com.example.softplanningapp.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.softplanningapp.R
import com.example.softplanningapp.SoftPlanningApplication
import com.example.softplanningapp.data.entities.NoteType
import com.example.softplanningapp.data.entities.Priority
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Date

class EditNoteFragment : Fragment() {

    private val args: EditNoteFragmentArgs by navArgs()
    private lateinit var editNoteViewModel: EditNoteViewModel

    // UI Elements
    private lateinit var textInputTitle: TextInputLayout
    private lateinit var textInputContent: TextInputLayout
    private lateinit var editTitle: TextInputEditText
    private lateinit var editContent: TextInputEditText
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioGroupPriority: RadioGroup
    private lateinit var locationSpinner: Spinner
    private lateinit var buttonSave: MaterialButton
    private lateinit var buttonDelete: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val application = requireActivity().application as SoftPlanningApplication
        val viewModelFactory = EditNoteViewModelFactory(application.repository, args.noteId)
        editNoteViewModel = ViewModelProvider(this, viewModelFactory)[EditNoteViewModel::class.java]

        initializeViews(view)
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        textInputTitle = view.findViewById(R.id.text_input_title)
        textInputContent = view.findViewById(R.id.text_input_content)
        editTitle = view.findViewById(R.id.edit_note_title)
        editContent = view.findViewById(R.id.edit_note_content)
        radioGroupType = view.findViewById(R.id.radio_group_type)
        radioGroupPriority = view.findViewById(R.id.radio_group_priority)
        locationSpinner = view.findViewById(R.id.spinner_location)
        buttonSave = view.findViewById(R.id.button_save_note)
        buttonDelete = view.findViewById(R.id.button_delete_note)
    }

    private fun setupObservers() {
        // Observe locations for spinner
        editNoteViewModel.allLocations.observe(viewLifecycleOwner) { locations ->
            val locationNames = mutableListOf("No location")
            locationNames.addAll(locations.map { it.name })

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                locationNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationSpinner.adapter = adapter

            // Set current selection after adapter is set
            editNoteViewModel.note.value?.let { note ->
                setLocationSpinnerSelection(note, locations)
            }
        }

        // Load the note data
        editNoteViewModel.note.observe(viewLifecycleOwner) { note ->
            note?.let {
                populateFields(it)
                // Set location spinner selection if locations are already loaded
                editNoteViewModel.allLocations.value?.let { locations ->
                    setLocationSpinnerSelection(note, locations)
                }
            }
        }

        // Observe save/delete results
        editNoteViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is EditNoteViewModel.OperationResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is EditNoteViewModel.OperationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLocationSpinnerSelection(note: com.example.softplanningapp.data.entities.Note, locations: List<com.example.softplanningapp.data.entities.Location>) {
        if (note.locationId != null) {
            val locationIndex = locations.indexOfFirst { it.id == note.locationId }
            if (locationIndex >= 0) {
                locationSpinner.setSelection(locationIndex + 1) // +1 because "No location" is first
            }
        } else {
            locationSpinner.setSelection(0) // "No location"
        }
    }

    private fun populateFields(note: com.example.softplanningapp.data.entities.Note) {
        // Populate text fields
        editTitle.setText(note.title)
        editContent.setText(note.content)

        // Select note type
        when (note.noteType) {
            NoteType.ACTIVITY -> radioGroupType.check(R.id.radio_activity)
            NoteType.IDEA_DUMP -> radioGroupType.check(R.id.radio_idea)
        }

        // Select priority
        when (note.priority) {
            Priority.HIGH -> radioGroupPriority.check(R.id.radio_priority_high)
            Priority.MEDIUM -> radioGroupPriority.check(R.id.radio_priority_medium)
            Priority.LOW -> radioGroupPriority.check(R.id.radio_priority_low)
        }
    }

    private fun setupClickListeners() {
        buttonSave.setOnClickListener {
            if (validateInput()) {
                saveNote()
            }
        }

        buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun validateInput(): Boolean {
        val title = editTitle.text.toString().trim()
        val content = editContent.text.toString().trim()

        // Clear previous errors
        textInputTitle.error = null
        textInputContent.error = null

        var isValid = true

        // Validate title
        if (title.isEmpty()) {
            textInputTitle.error = "Title is required"
            isValid = false
        } else if (title.length < 3) {
            textInputTitle.error = "Title must be at least 3 characters"
            isValid = false
        }

        // Validate content
        if (content.isEmpty()) {
            textInputContent.error = "Content is required"
            isValid = false
        } else if (content.length < 5) {
            textInputContent.error = "Content must be at least 5 characters"
            isValid = false
        }

        // Validate selections
        if (radioGroupType.checkedRadioButtonId == -1) {
            Toast.makeText(context, "Please select a note type", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (radioGroupPriority.checkedRadioButtonId == -1) {
            Toast.makeText(context, "Please select a priority", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveNote() {
        val title = editTitle.text.toString().trim()
        val content = editContent.text.toString().trim()

        val noteType = when (radioGroupType.checkedRadioButtonId) {
            R.id.radio_activity -> NoteType.ACTIVITY
            R.id.radio_idea -> NoteType.IDEA_DUMP
            else -> NoteType.ACTIVITY
        }

        val priority = when (radioGroupPriority.checkedRadioButtonId) {
            R.id.radio_priority_high -> Priority.HIGH
            R.id.radio_priority_medium -> Priority.MEDIUM
            R.id.radio_priority_low -> Priority.LOW
            else -> Priority.MEDIUM
        }

        // Get selected location (null if "No location" is selected)
        val selectedLocationId = if (locationSpinner.selectedItemPosition == 0) {
            null
        } else {
            editNoteViewModel.allLocations.value?.get(locationSpinner.selectedItemPosition - 1)?.id
        }

        editNoteViewModel.updateNote(title, content, noteType, priority, selectedLocationId)
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                editNoteViewModel.deleteNote()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}