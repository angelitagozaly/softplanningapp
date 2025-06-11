package com.example.softplanningapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.softplanningapp.data.entities.Note

@Dao
interface NoteDao {

    // ================== BASIC NOTE QUERIES ==================

    // Get all notes, newest first
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    // Get notes by type using string literals
    @Query("SELECT * FROM notes WHERE noteType = 'ACTIVITY' ORDER BY createdAt DESC")
    fun getActivityNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE noteType = 'IDEA_DUMP' ORDER BY createdAt DESC")
    fun getIdeaDumpNotes(): LiveData<List<Note>>

    // ================== NEW: GENERIC NOTE TYPE QUERIES ==================

    // Get notes by any type (flexible version)
    @Query("SELECT * FROM notes WHERE noteType = :noteType ORDER BY createdAt DESC")
    fun getNotesByType(noteType: String): LiveData<List<Note>>

    // Get completed notes
    @Query("SELECT * FROM notes WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedNotes(): LiveData<List<Note>>

    // Get incomplete notes
    @Query("SELECT * FROM notes WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getIncompleteNotes(): LiveData<List<Note>>

    // Get notes by priority
    @Query("SELECT * FROM notes WHERE priority = :priority ORDER BY createdAt DESC")
    fun getNotesByPriority(priority: String): LiveData<List<Note>>

    // Search notes by title or content
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): LiveData<List<Note>>

    // Get recent notes for home page
    @Query("SELECT * FROM notes ORDER BY createdAt DESC LIMIT 10")
    fun getRecentNotes(): LiveData<List<Note>>

    // Get recent notes with limit
    @Query("SELECT * FROM notes ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentNotesWithLimit(limit: Int): LiveData<List<Note>>

    // Simple count query
    @Query("SELECT COUNT(*) FROM notes")
    fun getNotesCount(): LiveData<Int>

    // ================== LOCATION-BASED QUERIES ==================

    // Get active notes that have locations (for geofencing)
    @Query("SELECT * FROM notes WHERE isCompleted = 0 AND locationId IS NOT NULL")
    fun getActiveNotesWithLocation(): LiveData<List<Note>>

    // Get notes for a specific location
    @Query("SELECT * FROM notes WHERE locationId = :locationId AND isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getNotesForLocation(locationId: Long): LiveData<List<Note>>

    // Get all notes for a specific location (including completed)
    @Query("SELECT * FROM notes WHERE locationId = :locationId ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllNotesForLocation(locationId: Long): LiveData<List<Note>>

    // Get notes with location info (JOIN query for Day 4 geofencing)
    @Query("""
        SELECT notes.*, locations.name as locationName, locations.latitude, locations.longitude, locations.radius 
        FROM notes 
        INNER JOIN locations ON notes.locationId = locations.id 
        WHERE notes.isCompleted = 0 AND locations.isActive = 1
        ORDER BY notes.priority DESC, notes.createdAt DESC
    """)
    fun getActiveNotesWithLocationDetails(): LiveData<List<NoteWithLocation>>

    // Count notes by location
    @Query("SELECT COUNT(*) FROM notes WHERE locationId = :locationId AND isCompleted = 0")
    fun getActiveNoteCountForLocation(locationId: Long): LiveData<Int>

    // ================== CRUD OPERATIONS ==================

    // Insert new note
    @Insert
    suspend fun insertNote(note: Note): Long

    // Update existing note
    @Update
    suspend fun updateNote(note: Note)

    // Delete note
    @Delete
    suspend fun deleteNote(note: Note)

    // ================== NEW: COMPLETION STATUS METHODS ==================

    // Mark note as completed (original method)
    @Query("UPDATE notes SET isCompleted = 1, updatedAt = :completedTime WHERE id = :noteId")
    suspend fun markNoteAsCompleted(noteId: Long, completedTime: Long = System.currentTimeMillis())

    // Mark note as complete (alias for compatibility)
    @Query("UPDATE notes SET isCompleted = 1, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun markNoteComplete(noteId: Long, updatedAt: Long = System.currentTimeMillis())

    // Mark note as incomplete
    @Query("UPDATE notes SET isCompleted = 0, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun markNoteIncomplete(noteId: Long, updatedAt: Long = System.currentTimeMillis())

    // ================== INDIVIDUAL NOTE QUERIES ==================

    // Get note by ID (LiveData)
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteByIdLiveData(id: Long): LiveData<Note?>

    // Get note by ID (suspend for immediate access)
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    // Check if note exists
    @Query("SELECT COUNT(*) FROM notes WHERE id = :id")
    fun noteExistsLiveData(id: Long): LiveData<Int>

    // ================== NEW: UTILITY METHODS ==================

    // Get notes with reminders
    @Query("SELECT * FROM notes WHERE reminderTime IS NOT NULL AND reminderTime > :currentTime ORDER BY reminderTime ASC")
    fun getNotesWithUpcomingReminders(currentTime: Long): LiveData<List<Note>>

    // Update note priority
    @Query("UPDATE notes SET priority = :priority, previousPriority = :previousPriority, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNotePriority(noteId: Long, priority: String, previousPriority: String?, updatedAt: Long = System.currentTimeMillis())

    // Delete all completed notes
    @Query("DELETE FROM notes WHERE isCompleted = 1")
    suspend fun deleteAllCompletedNotes()

    // Get note count by type
    @Query("SELECT COUNT(*) FROM notes WHERE noteType = :noteType")
    suspend fun getNoteCountByType(noteType: String): Int

    // Get incomplete note count
    @Query("SELECT COUNT(*) FROM notes WHERE isCompleted = 0")
    suspend fun getIncompleteNoteCount(): Int
}

// Data class for JOIN queries (for geofencing in Day 4)
data class NoteWithLocation(
    val id: Long,
    val title: String,
    val content: String,
    val noteType: String,
    val locationId: Long,
    val isCompleted: Boolean,
    val priority: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)