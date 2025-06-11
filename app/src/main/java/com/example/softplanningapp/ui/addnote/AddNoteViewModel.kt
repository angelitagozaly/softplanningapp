package com.example.softplanningapp.ui.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.softplanningapp.data.entities.Location
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.data.entities.NoteType
import com.example.softplanningapp.data.entities.Priority
import com.example.softplanningapp.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Date

class AddNoteViewModel(private val repository: AppRepository) : ViewModel() {

    // Get all locations for the spinner
    val allLocations: LiveData<List<Location>> = repository.getAllActiveLocations()

    // Live data for form validation and results
    private val _titleError = MutableLiveData<String?>()
    val titleError: LiveData<String?> = _titleError

    private val _contentError = MutableLiveData<String?>()
    val contentError: LiveData<String?> = _contentError

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    fun saveNote(
        title: String,
        content: String,
        noteType: NoteType,
        priority: Priority,
        locationId: Long?
    ) {
        // Clear previous errors
        _titleError.value = null
        _contentError.value = null

        // Validate input
        var isValid = true

        if (title.isBlank()) {
            _titleError.value = "Title is required"
            isValid = false
        }

        if (content.isBlank()) {
            _contentError.value = "Content is required"
            isValid = false
        }

        if (!isValid) {
            return
        }

        // Create the note
        val currentTime = Date()
        val note = Note(
            title = title,
            content = content,
            noteType = noteType,
            locationId = locationId,
            isCompleted = false,
            createdAt = currentTime,
            updatedAt = currentTime,
            reminderTime = null, // We can add reminder functionality later
            priority = priority
        )

        // Save to database
        viewModelScope.launch {
            try {
                val noteId = repository.insertNote(note)
                _saveResult.value = noteId > 0
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
}

class AddNoteViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddNoteViewModel::class.java)) {
            return AddNoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}