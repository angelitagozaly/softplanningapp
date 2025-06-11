package com.example.softplanningapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.softplanningapp.data.entities.NotificationLog
import com.example.softplanningapp.data.entities.UserAction

@Dao
interface NotificationLogDao {

    // Get all notification history
    @Query("SELECT * FROM notification_logs ORDER BY triggeredAt DESC")
    fun getAllNotificationLogs(): LiveData<List<NotificationLog>>

    // Get recent notifications for home page
    @Query("SELECT * FROM notification_logs ORDER BY triggeredAt DESC LIMIT :limit")
    fun getRecentNotifications(limit: Int): LiveData<List<NotificationLog>>

    // Get logs for a specific note (to see how often it's suggested)
    @Query("SELECT * FROM notification_logs WHERE noteId = :noteId ORDER BY triggeredAt DESC")
    fun getLogsForNote(noteId: Long): LiveData<List<NotificationLog>>

    // Count accepted vs dismissed for analytics
    @Query("SELECT COUNT(*) FROM notification_logs WHERE userAction = :action")
    suspend fun getActionCount(action: UserAction): Int

    // Insert new notification log
    @Insert
    suspend fun insertNotificationLog(log: NotificationLog): Long

    // Update user action when they respond to notification
    @Query("UPDATE notification_logs SET userAction = :action, actionTime = :actionTime WHERE id = :logId")
    suspend fun updateUserAction(logId: Long, action: UserAction, actionTime: java.util.Date)

    // Delete old logs (for cleanup)
    @Query("DELETE FROM notification_logs WHERE triggeredAt < :cutoffDate")
    suspend fun deleteOldLogs(cutoffDate: java.util.Date)
}