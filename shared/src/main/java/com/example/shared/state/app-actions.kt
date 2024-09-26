package com.example.shared.state

import com.example.shared.model.Bookmarks

sealed class AppAction {
    data object LoadBookmarks : AppAction()
    data class SetBookmarks(val bookmarks: List<Bookmarks>) : AppAction()
    data class BookmarksLoaded(val bookmarks: List<Bookmarks>) : AppAction()
    data class AddBookmark(val bookmark: Bookmarks) : AppAction()
    data class RemoveBookmark(val bookmarkId: String) : AppAction()
    data class UpdateBookmark(val index: Int, val title: String) : AppAction()
    data class ErrorOccurred(val message: String) : AppAction()
    data class UpdateItem(
        val index: Int,
        val itemId: String,
        val data : Map<String,Any>,
        val itemType: ItemType
    ) : AppAction()
}

enum class ItemType {
    BOOKMARK,
    COLLECTION
}