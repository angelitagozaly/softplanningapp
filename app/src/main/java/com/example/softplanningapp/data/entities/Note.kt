package com.example.softplanningapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL // If location is deleted, set locationId to null
        )
    ],
    indices = [Index(value = ["locationId"])] // Index for better query performance
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val noteType: NoteType,
    val locationId: Long? = null,  // Foreign key to Location entity
    val isCompleted: Boolean = false,
    val createdAt: Date,
    val updatedAt: Date,
    val reminderTime: Date? = null,
    val priority: Priority = Priority.MEDIUM,
    val previousPriority: Priority? = null
)

enum class NoteType {
    ACTIVITY,      // Has context (location/time) - "Buy milk when near store"
    IDEA_DUMP      // Just ideas without context - "Maybe learn Spanish someday"
}

enum class Priority(val displayName: String, val value: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3)
}