package com.example.shared.state

import com.example.shared.model.Bookmarks
import com.example.shared.model.Collections

data class AppState(
    val bookmarks: List<Bookmarks> = emptyList(),
    val collections:List<Collections> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)