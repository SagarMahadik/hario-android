package com.example.shared.state

import com.example.shared.model.Bookmarks
import com.example.shared.model.Collections

data class Auth(
    val isAuthenticated: Boolean = false,
    val loginRequired: Boolean = false
)

data class AppState(
    val bookmarks: List<Bookmarks> = emptyList(),
    val collections:List<Collections> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val auth: Auth = Auth() // Default value for the Auth class
)