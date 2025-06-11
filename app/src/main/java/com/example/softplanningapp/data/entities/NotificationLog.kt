package com.example.softplanningapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,                    // Which note triggered this
    val locationId: Long,                // Where user was when triggered
    val triggeredAt: Date,               // When notification was sent
    val userAction: UserAction,          // What user did
    val actionTime: Date? = null         // When user responded
)

enum class UserAction {
    ACCEPTED,     // User clicked "Do it now"
    DISMISSED,    // User dismissed notification
    SNOOZED,      // User chose "Remind me later"
    NO_ACTION     // No action taken (notification expired)
}