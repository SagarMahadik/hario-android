package com.example.shared.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.shared.db.entity.BookmarkEntity
import com.example.shared.db.entity.BookmarkWithHighlights
import kotlinx.coroutines.flow.Flow
import com.example.shared.db.refs.BookmarkTagRef

data class PartialBookmarkEntity(
    @PrimaryKey val _id: String,
    val title: String? = null,
    val url: String? = null,
    val isFavorite: Boolean? = null
)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarkTagRefs(refs: List<BookmarkTagRef>)

    @Query("DELETE FROM bookmark_tag_ref WHERE bookmarkId = :bookmarkId")
    suspend fun deleteBookmarkTagRefs(bookmarkId: String)

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Transaction
    @Query("SELECT * FROM bookmarks")
    suspend fun getBookmarksWithHighlights(): List<BookmarkWithHighlights>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE _id = :bookmarkId")
    suspend fun getBookmarkWithHighlights(bookmarkId: String): BookmarkWithHighlights?
}
