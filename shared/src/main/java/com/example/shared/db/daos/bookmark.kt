package com.example.shared.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.shared.db.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

data class PartialBookmarkEntity(
    @PrimaryKey val _id: String,
    val title: String? = null,
    val url: String? = null,
    val isFavorite: Boolean? = null
)


// File: shared/kotlin+java/com.example.shared/db/dao/BookmarkDao.kt
@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("SELECT * FROM bookmarks WHERE _id = :id")
    suspend fun getBookmarkById(id: String): BookmarkEntity?

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)
}
