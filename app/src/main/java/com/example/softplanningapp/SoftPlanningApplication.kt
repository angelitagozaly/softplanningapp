package com.example.softplanningapp

import android.app.Application
import com.example.softplanningapp.data.database.AppDatabase
import com.example.softplanningapp.data.repository.AppRepository

class SoftPlanningApplication : Application() {

    // Database instance - created only when first accessed
    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    // Repository instance - provides clean access to data
    val repository by lazy {
        AppRepository(
            database.noteDao(),
            database.locationDao()
        )
    }
}