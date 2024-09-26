package com.example.shared.db

import android.content.Context
import androidx.room.Room
import com.example.shared.db.daos.BookmarkDao
import com.example.shared.db.repository.BookmarkRepository
import com.example.shared.model.Bookmarks
import kotlinx.coroutines.flow.Flow

class DbManager private constructor(context: Context) {
    private val database: AppDatabase
    val bookmarkRepository: BookmarkRepository

    init {
        database = AppDatabase.getInstance(context.applicationContext)
        val bookmarkDao: BookmarkDao = database.bookmarkDao()
        bookmarkRepository = BookmarkRepository(bookmarkDao)
    }

    companion object {
        @Volatile
        private var INSTANCE: DbManager? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = DbManager(context)
                    }
                }
            }
        }

        fun getInstance(): DbManager {
            return INSTANCE ?: throw IllegalStateException("DbManager must be initialized in Application class before use.")
        }
    }

    suspend fun mutate(payload: MutationPayload) {
        when (payload.operation) {
            "add" -> handleAddOperation(payload)
            "update" -> handleUpdateOperation(payload)
            "delete" -> handleDeleteOperation(payload)
            else -> throw IllegalArgumentException("Unsupported operation: ${payload.operation}")
        }
    }

    private suspend fun handleAddOperation(payload: MutationPayload) {
        when (payload.collection) {
            "bookmarks" -> {
                val bookmark = payload.data.toBookmark()
                bookmarkRepository.insertBookmarks(listOf(bookmark))
            }
            else -> throw IllegalArgumentException("Unsupported collection: ${payload.collection}")
        }
    }

    private suspend fun handleUpdateOperation(payload: MutationPayload) {
        // TODO: Implement update logic here
    }

    private suspend fun handleDeleteOperation(payload: MutationPayload) {
        // TODO: Implement delete logic here
    }

    // Helper functions to convert Map<String, Any> to entity objects
    private fun Map<String, Any>.toBookmark(): Bookmarks {
        return Bookmarks(
            _id = this["_id"] as? String ?: "",
            title = this["title"] as? String ?: "",
            url = this["url"] as? String ?: "",
            isFavorite = this["isFavorite"] as? Boolean ?: false
        )
    }

    // Expose methods to get data from repositories
    fun getAllBookmarksFlow(): Flow<List<Bookmarks>> = bookmarkRepository.getAllBookmarksFlow()
    suspend fun getAllBookmarks(): List<Bookmarks> = bookmarkRepository.getAllBookmarks()

    // Add similar methods for other entities (Collections, Tags, Highlights)
}

data class MutationPayload(
    val operation: String,
    val arrayOperation: String? = null,
    val collection: String,
    val data: Map<String, Any>
)