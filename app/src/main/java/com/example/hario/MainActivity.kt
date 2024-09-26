package com.example.hario
import com.example.shared.SharedVariables
import com.example.hario.BuildConfig

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hario.databinding.ActivityMainBinding
import com.example.shared.ConfigHandler
import com.example.shared.db.AppDatabase
import com.example.shared.db.DbManager
import com.example.shared.db.repository.BookmarkRepository
import com.example.shared.model.Bookmarks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bookmarkRepository: BookmarkRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConfigHandler.initialize(
            apiEndpoint = BuildConfig.API_ENDPOINT,
            environment = BuildConfig.ENVIRONMENT
        )

        println("API Endpoint: ${ConfigHandler.API_ENDPOINT}")
        println("Environment: ${ConfigHandler.ENVIRONMENT}")

        println(SharedVariables.STATIC_VARIABLE)

        // Access and modify the mutable variable
        println("Mobile App - Before: ${SharedVariables.mutableVariable}")
        SharedVariables.mutableVariable = "Updated from Mobile App"
        println("Mobile App - After: ${SharedVariables.mutableVariable}")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         bookmarkRepository = DbManager.getInstance().bookmarkRepository

        // Load sample data
        loadSampleData()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun loadSampleData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sampleBookmarks = generateSampleBookmarks()
            bookmarkRepository.insertBookmarks(sampleBookmarks)
            println("Sample data loaded successfully")
        }
    }

    private fun generateSampleBookmarks(): List<Bookmarks> {
        return listOf(
            Bookmarks("1", "Google", "https://www.google.com", false),
            Bookmarks("2", "GitHub", "https://github.com", true),
            Bookmarks("3", "Stack Overflow", "https://stackoverflow.com", false),
            Bookmarks("4", "Kotlin Official", "https://kotlinlang.org", true),
            Bookmarks("5", "Android Developers", "https://developer.android.com", false)
        )
    }
}