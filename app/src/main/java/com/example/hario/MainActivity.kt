package com.example.hario
import com.example.shared.SharedVariables
import com.example.hario.BuildConfig

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hario.databinding.ActivityMainBinding
import com.example.shared.ConfigHandler


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
}