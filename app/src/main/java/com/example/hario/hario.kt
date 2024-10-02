package com.example.hario

import android.app.Application
import com.example.shared.api.SessionManager
import com.example.shared.db.DbManager
import com.example.shared.db.RealmDbManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize DbManager with the application context
        DbManager.initialize(this)
        SessionManager.initialize(this)
    }
}
