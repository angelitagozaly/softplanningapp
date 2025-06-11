package com.example.softplanningapp.ui.notes

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

class EditNoteViewModel(
    private val repository: AppRepository,
    private val noteId: Long
) : ViewModel() {

    // Get all locations for the spinner
    val allLocations: LiveData<List<Location>> = repository.getAllActiveLocations()

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _operationResult = MutableLiveData<OperationResult>()
    val operationResult: LiveData<OperationResult> = _operationResult

    sealed class OperationResult {
        data class Success(val message: String) : OperationResult()
        data class Error(val message: String) : OperationResult()
    }

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                val loadedNote = repository.getNoteById(noteId)
                _note.value = loadedNote
            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error("Failed to load note")
            }
        }
    }

    fun updateNote(
        title: String,
        content: String,
        noteType: NoteType,
        priority: Priority,
        locationId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val currentNote = _note.value
                if (currentNote != null) {
                    val updatedNote = currentNote.copy(
                        title = title,
                        content = content,
                        noteType = noteType,
                        priority = priority,
                        locationId = locationId,
                        updatedAt = Date()
                    )
                    repository.updateNote(updatedNote)
                    _operationResult.value = OperationResult.Success("Note updated successfully!")
                } else {
                    _operationResult.value = OperationResult.Error("Note not found")
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error("Failed to update note: ${e.message}")
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                val currentNote = _note.value
                if (currentNote != null) {
                    repository.deleteNote(currentNote)
                    _operationResult.value = OperationResult.Success("Note deleted successfully!")
                } else {
                    _operationResult.value = OperationResult.Error("Note not found")
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error("Failed to delete note: ${e.message}")
            }
        }
    }
}

class EditNoteViewModelFactory(
    private val repository: AppRepository,
    private val noteId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            return EditNoteViewModel(repository, noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}