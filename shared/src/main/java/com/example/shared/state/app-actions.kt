package com.example.shared.state

import com.example.shared.model.Bookmarks
import com.example.shared.model.Collection
import com.example.shared.model.Tag
import com.example.shared.model.Settings
import com.example.shared.model.User

sealed class AppAction {
    data object LoadBookmarks : AppAction()
    data class SetBookmarks(val bookmarks: List<Bookmarks>) : AppAction()
    data class SetCollections(val collections: List<Collection>) : AppAction()
    data class SetTags(val tags: List<Tag>) : AppAction()
    data class SetSettings(val settings: Settings) : AppAction()
    data class SetUser(val user: User) : AppAction()
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
    object LoginRequired : AppAction()
    object LoginSuccess : AppAction()
}

enum class ItemType {
    BOOKMARK,
    COLLECTION
}
fun mapItemTypeToString(itemType: ItemType): String {
        return when (itemType) {
            ItemType.BOOKMARK -> "bookmarks"
            ItemType.COLLECTION -> "collections"
        }
    
}
