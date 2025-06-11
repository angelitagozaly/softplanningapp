package com.example.softplanningapp.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.data.entities.NoteType
import com.example.softplanningapp.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Date

class NotesViewModel(private val repository: AppRepository) : ViewModel() {

    // All notes from database
    val allNotes: LiveData<List<Note>> = repository.getAllNotes()

    // Current search query
    private val _searchQuery = MutableLiveData("")
    private val searchQuery: LiveData<String> = _searchQuery

    // Current filter type (now includes completion status)
    private val _filterType = MutableLiveData<FilterType>(FilterType.ALL)
    private val filterType: LiveData<FilterType> = _filterType

    // Filtered notes based on search and filter
    val filteredNotes = MediatorLiveData<List<Note>>().apply {
        addSource(allNotes) { notes ->
            value = applyFilters(notes, searchQuery.value ?: "", filterType.value ?: FilterType.ALL)
        }
        addSource(searchQuery) { query ->
            value = applyFilters(allNotes.value ?: emptyList(), query, filterType.value ?: FilterType.ALL)
        }
        addSource(filterType) { type ->
            value = applyFilters(allNotes.value ?: emptyList(), searchQuery.value ?: "", type ?: FilterType.ALL)
        }
    }

    fun searchNotes(query: String) {
        _searchQuery.value = query
    }

    fun filterByType(type: FilterType) {
        _filterType.value = type
    }

    fun toggleNoteCompletion(note: Note) {
        viewModelScope.launch {
            try {
                val updatedNote = if (note.isCompleted) {
                    // Undo completion - restore previous priority
                    note.copy(
                        isCompleted = false,
                        priority = note.previousPriority ?: note.priority,
                        previousPriority = null,
                        updatedAt = Date()
                    )
                } else {
                    // Mark as completed - save current priority
                    note.copy(
                        isCompleted = true,
                        previousPriority = note.priority,
                        updatedAt = Date()
                    )
                }
                repository.updateNote(updatedNote)
            } catch (e: Exception) {
                // Handle error - could show a toast or error state
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                // Handle error - could show a toast or error state
            }
        }
    }

    private fun applyFilters(notes: List<Note>, query: String, type: FilterType): List<Note> {
        var filteredNotes = notes

        // Apply search filter
        if (query.isNotBlank()) {
            filteredNotes = filteredNotes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
            }
        }

        // Apply type/status filter
        filteredNotes = when (type) {
            FilterType.ALL -> filteredNotes
            FilterType.ONGOING -> filteredNotes.filter { !it.isCompleted }
            FilterType.ACTIVITY -> filteredNotes.filter { it.noteType == NoteType.ACTIVITY && !it.isCompleted }
            FilterType.IDEA -> filteredNotes.filter { it.noteType == NoteType.IDEA_DUMP && !it.isCompleted }
            FilterType.COMPLETED -> filteredNotes.filter { it.isCompleted }
        }

        return filteredNotes
    }
}

enum class FilterType {
    ALL,
    ONGOING,
    ACTIVITY,
    IDEA,
    COMPLETED
}

class NotesViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}