package com.example.softplanningapp.data.repository

import androidx.lifecycle.LiveData
import com.example.softplanningapp.data.dao.NoteDao
import com.example.softplanningapp.data.dao.LocationDao
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.data.entities.Location

class AppRepository(
    private val noteDao: NoteDao,
    private val locationDao: LocationDao
) {

    // === NOTE METHODS ===
    fun getAllNotes(): LiveData<List<Note>> = noteDao.getAllNotes()

    fun getNotesByType(noteType: String): LiveData<List<Note>> =
        noteDao.getNotesByType(noteType)

    fun getCompletedNotes(): LiveData<List<Note>> = noteDao.getCompletedNotes()

    fun getIncompleteNotes(): LiveData<List<Note>> = noteDao.getIncompleteNotes()

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun markNoteComplete(noteId: Long) = noteDao.markNoteComplete(noteId)

    suspend fun markNoteIncomplete(noteId: Long) = noteDao.markNoteIncomplete(noteId)

    fun searchNotes(query: String): LiveData<List<Note>> = noteDao.searchNotes(query)

    // === LOCATION METHODS ===
    fun getAllActiveLocations(): LiveData<List<Location>> = locationDao.getAllActiveLocations()

    fun getAllLocations(): LiveData<List<Location>> = locationDao.getAllLocations()

    suspend fun getLocationByIdDirect(id: Long): Location = locationDao.getLocationByIdDirect(id)

    suspend fun locationExists(id: Long): Int = locationDao.locationExists(id)

    fun searchLocations(query: String): LiveData<List<Location>> = locationDao.searchLocations(query)

    suspend fun getActiveLocationsForGeofencing(): List<Location> =
        locationDao.getActiveLocationsForGeofencing()

    suspend fun insertLocation(location: Location): Long = locationDao.insertLocation(location)

    suspend fun updateLocation(location: Location) = locationDao.updateLocation(location)

    suspend fun deleteLocation(location: Location) = locationDao.deleteLocation(location)

    suspend fun deactivateLocation(locationId: Long): Int = locationDao.deactivateLocation(locationId)

    suspend fun reactivateLocation(locationId: Long): Int = locationDao.reactivateLocation(locationId)
}