package com.example.shared.state

import com.example.shared.model.Bookmarks
import com.example.shared.model.Collection
import com.example.shared.model.Settings
import com.example.shared.model.Tag
import com.example.shared.model.User
import java.util.Date

data class Auth(
    val isAuthenticated: Boolean = false,
    val loginRequired: Boolean = false
)

data class InitialSettings(
    val dock: Settings.Dock = Settings.Dock(1.0f),
    val sidebar: Settings.Sidebar = Settings.Sidebar("left_pinned", 0.7f),
    val userId: String = "",
    val updatedAt: Date? = null
)

data class AppState(
    val bookmarks: List<Bookmarks> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val auth: Auth = Auth(),
    val settings: Settings = Settings(
        _id = "",
        dock = InitialSettings().dock,
        sidebar = InitialSettings().sidebar,
        userId = InitialSettings().userId,
        updatedAt = InitialSettings().updatedAt
    ),
    val user: User? = null
)