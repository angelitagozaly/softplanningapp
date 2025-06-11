package com.example.softplanningapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var isUpdatingBottomNav = false // Flag to prevent infinite loop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up bottom navigation with custom behavior
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Custom bottom navigation setup to clear back stack
        bottomNavView.setOnItemSelectedListener { item ->
            // Prevent infinite loop when we're updating programmatically
            if (isUpdatingBottomNav) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.homeFragment -> {
                    // Clear back stack and navigate to home
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.notesFragment -> {
                    // Clear back stack and navigate to notes
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.notesFragment)
                    true
                }
                R.id.locationsFragment -> {
                    // Clear back stack and navigate to locations
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.locationsFragment)
                    true
                }
                else -> false
            }
        }

        // Listen for destination changes to update bottom navigation selection
        navController.addOnDestinationChangedListener { _, destination, _ ->
            isUpdatingBottomNav = true // Set flag to prevent loop

            when (destination.id) {
                R.id.homeFragment -> {
                    bottomNavView.selectedItemId = R.id.homeFragment
                }
                R.id.notesFragment -> {
                    bottomNavView.selectedItemId = R.id.notesFragment
                }
                R.id.locationsFragment -> {
                    bottomNavView.selectedItemId = R.id.locationsFragment
                }
                // For other destinations (AddNote, EditNote, AddLocation), don't change selection
            }

            isUpdatingBottomNav = false // Clear flag
        }

        // Set up action bar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.notesFragment,
                R.id.locationsFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Handle notification intent
        handleNotificationIntent()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun handleNotificationIntent() {
        val fromNotification = intent.getBooleanExtra("from_notification", false)
        val noteId = intent.getLongExtra("note_id", -1L)

        if (fromNotification && noteId != -1L) {
            // Navigate to the specific note when coming from notification
            // This will be implemented when we add geofencing
        }
    }
}