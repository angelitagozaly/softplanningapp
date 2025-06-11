package com.example.softplanningapp.data.database

import androidx.room.TypeConverter
import com.example.softplanningapp.data.entities.NoteType
import com.example.softplanningapp.data.entities.Priority
import com.example.softplanningapp.data.entities.UserAction
import java.util.Date

class Converters {

    // Convert Date to Long and back (Room can store Long but not Date)
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Convert Enums to String and back
    @TypeConverter
    fun fromNoteType(noteType: NoteType): String {
        return noteType.name
    }

    @TypeConverter
    fun toNoteType(noteType: String): NoteType {
        return NoteType.valueOf(noteType)
    }

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

    @TypeConverter
    fun fromUserAction(userAction: UserAction): String {
        return userAction.name
    }

    @TypeConverter
    fun toUserAction(userAction: String): UserAction {
        return UserAction.valueOf(userAction)
    }
}