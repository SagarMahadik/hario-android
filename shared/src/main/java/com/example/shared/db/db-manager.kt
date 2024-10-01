package com.example.shared.db

import android.content.Context
import com.example.shared.api.ApiManager
import com.example.shared.api.SyncRecord
import com.example.shared.db.daos.BookmarkDao
import com.example.shared.db.repository.BookmarkRepository
import com.example.shared.db.repository.CollectionRepository
import com.example.shared.db.repository.HighlightRepository
import com.example.shared.db.repository.TagRepository
import com.example.shared.db.repository.toPartialCollectionEntity
import com.example.shared.model.Bookmarks
import com.example.shared.model.Collection
import com.example.shared.model.Tag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import org.json.JSONArray

//NOTE: Move this to common module
data class MutationPayload(
    val operation: String,
    val arrayOperation: String? = null,
    val collection: String,
    val data: Map<String, Any>
)

class DbManager private constructor(context: Context) {
    private val database: AppDatabase
    private val gson = Gson()

    val bookmarkRepository: BookmarkRepository
    val highlightRepository: HighlightRepository
    val collectionRepository: CollectionRepository
    val tagRepository: TagRepository


    init {
        database = AppDatabase.getInstance(context.applicationContext)
        val bookmarkDao: BookmarkDao = database.bookmarkDao()
        bookmarkRepository = BookmarkRepository(bookmarkDao)

        val collectionsDao = database.collectionDao()
        collectionRepository = CollectionRepository(collectionsDao)

        val tagsDao = database.tagDao()
        tagRepository = TagRepository(tagsDao)

        val highlightsDao = database.highlightDao()
        highlightRepository = HighlightRepository(highlightsDao)
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
            return INSTANCE
                ?: throw IllegalStateException("DbManager must be initialized in Application class before use.")
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

    suspend fun applyDeltaChanges(syncRecords: List<SyncRecord>) {
        val sortedRecords = syncRecords.sortedBy { it.syncId }

        for (record in sortedRecords) {
            val dataMap = parseJsonToMap(record.data)
            val payload = MutationPayload(
                operation = record.operation,
                collection = record.collection,
                data = dataMap,
            )
            mutate(payload)
        }
    }

    private fun parseJsonToMap(jsonString: String): Map<String, Any> {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    private suspend fun handleUpdateOperation(payload: MutationPayload) {
        when (payload.collection) {
            "bookmarks" -> {
                print("Bookmarks")
            }

            "collections" -> {
                val id = payload.data["_id"] as? String
                    ?: throw IllegalArgumentException("_id is required for updating a collection")
                val partialCollection = payload.data.toPartialCollectionEntity()
                collectionRepository.updateCollectionById(id, partialCollection)
            }

            "highlights" -> {
                print("Highlights")
            }

            "tags" -> {
                print("Tags")
            }

            else -> throw IllegalArgumentException("Unsupported collection: ${payload.collection}")
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

    fun getCollectionsFlow(): Flow<List<Collection>> = collectionRepository.getAllCollectionsFlow()
    fun getTagsFlow(): Flow<List<Tag>> = tagRepository.getAllTagsFlow()

    // Add similar methods for other entities (Collections, Tags, Highlights)
}

fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    this.keys().forEach { key ->
        val value = this.get(key)
        when (value) {
            is JSONObject -> map[key] = value.toMap()
            is JSONArray -> map[key] = value.toList()
            JSONObject.NULL -> map[key] = null as Any
            else -> map[key] = value
        }
    }
    return map
}

// Extension function to convert JSONArray to List<Any>
fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        when (val value = this.get(i)) {
            is JSONObject -> list.add(value.toMap())
            is JSONArray -> list.add(value.toList())
            JSONObject.NULL -> list.add(null as Any)
            else -> list.add(value)
        }
    }
    return list
}