package com.example.softplanningapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.softplanningapp.data.dao.LocationDao
import com.example.softplanningapp.data.dao.NoteDao
import com.example.softplanningapp.data.dao.NotificationLogDao
import com.example.softplanningapp.data.entities.Location
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.data.entities.NotificationLog

@Database(
    entities = [Note::class, Location::class, NotificationLog::class],
    version = 3, // Incremented for foreign key addition
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Abstract functions to get DAOs
    abstract fun noteDao(): NoteDao
    abstract fun locationDao(): LocationDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 2 to 3 (adding foreign key)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite doesn't support adding foreign keys to existing tables
                // We need to recreate the table with the foreign key constraint

                // 1. Create new table with foreign key
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notes_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `noteType` TEXT NOT NULL,
                        `locationId` INTEGER,
                        `isCompleted` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `reminderTime` INTEGER,
                        `priority` TEXT NOT NULL,
                        `previousPriority` TEXT,
                        FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())

                // 2. Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO `notes_new` 
                    SELECT `id`, `title`, `content`, `noteType`, `locationId`, `isCompleted`, 
                           `createdAt`, `updatedAt`, `reminderTime`, `priority`, `previousPriority`
                    FROM `notes`
                """.trimIndent())

                // 3. Drop old table
                database.execSQL("DROP TABLE `notes`")

                // 4. Rename new table to original name
                database.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")

                // 5. Create index for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_locationId` ON `notes` (`locationId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            // If database already exists, return it
            return INSTANCE ?: synchronized(this) {
                // Create database if it doesn't exist
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soft_planning_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // For development - recreates DB on schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}