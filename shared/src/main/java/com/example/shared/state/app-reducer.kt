package com.example.shared.state

import org.reduxkotlin.Reducer

val appReducer: Reducer<AppState> = { state, action ->
    when (action) {
        is AppAction.LoadBookmarks -> state.copy(isLoading = true)

        is AppAction.BookmarksLoaded -> state.copy(
            isLoading = false,
            bookmarks = action.bookmarks
        )

        is AppAction.SetBookmarks -> state.copy(bookmarks = action.bookmarks)

        is AppAction.AddBookmark -> state.copy(
            bookmarks = state.bookmarks + action.bookmark
        )

        is AppAction.RemoveBookmark -> state.copy(
            bookmarks = state.bookmarks.filterNot { it._id == action.bookmarkId }
        )

        is AppAction.ErrorOccurred -> state.copy(
            isLoading = false,
            errorMessage = action.message
        )

        is AppAction.LoginRequired -> {
            state.copy(auth = state.auth.copy(loginRequired = true))
        }

        is AppAction.LoginSuccess -> {
            state.copy(auth = state.auth.copy(loginRequired = false,
                isAuthenticated = true
            ))
        }

        is AppAction.UpdateBookmark -> {
            val updatedBookmarks = state.bookmarks.mapIndexed { idx, bookmark ->
                if (idx == action.index) {
                    bookmark.copy(title = action.title)
                } else {
                    bookmark
                }
            }
            state.copy(bookmarks = updatedBookmarks)
        }

        is AppAction.UpdateItem -> {
            when (action.itemType) {
                ItemType.BOOKMARK -> {
                    val updatedBookmarks = state.bookmarks.toMutableList()
                    if (action.index in updatedBookmarks.indices
                    ) {
                        val bookmark = updatedBookmarks[action.index]
                        updatedBookmarks[action.index] = bookmark.update(action.data)
                    } else {
                        // Handle error: index out of bounds or ID mismatch
                    }
                    state.copy(bookmarks = updatedBookmarks)
                }
                ItemType.COLLECTION -> {
                    val updatedCollections = state.collections.toMutableList()
                    if (action.index in updatedCollections.indices
                    ) {
                        val collection = updatedCollections[action.index]
                        updatedCollections[action.index] = collection.update(action.data)
                    } else {
                        // Handle error: index out of bounds or ID mismatch
                    }
                    state.copy(collections = updatedCollections)
                }
            }
        }

        else -> state
    }
}