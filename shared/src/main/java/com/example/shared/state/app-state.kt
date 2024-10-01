package com.example.shared.state

import com.example.shared.model.Bookmarks
import com.example.shared.model.Collection
import com.example.shared.model.Tag

data class Auth(
    val isAuthenticated: Boolean = false,
    val loginRequired: Boolean = false
)

data class AppState(
    val bookmarks: List<Bookmarks> = emptyList(),
    val collections:List<Collection> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val auth: Auth = Auth() // Default value for the Auth class
)