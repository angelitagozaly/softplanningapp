package com.example.softplanningapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    // Get all notes from repository
    private val allNotes: LiveData<List<Note>> = repository.getAllNotes()

    // Transform the data for UI
    val totalNotes: LiveData<Int> = allNotes.map { notes ->
        notes.size
    }

    val completedNotes: LiveData<Int> = allNotes.map { notes ->
        notes.count { it.isCompleted }
    }

    val activeNotes: LiveData<Int> = allNotes.map { notes ->
        notes.count { !it.isCompleted }
    }

    // Recent notes (excluding completed ones)
    val recentNotes: LiveData<List<Note>> = allNotes.map { notes ->
        notes.filter { !it.isCompleted }
            .sortedByDescending { it.createdAt }
            .take(5)
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
}

class HomeViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}