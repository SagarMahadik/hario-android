package com.example.shared.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.example.shared.db.entity.HighlightEntity
import com.example.shared.db.entity.HighlightWithBookmark
import com.example.shared.db.refs.HighlightTagRef

data class PartialHighlight(
    @PrimaryKey val _id: String,
    val title: String? = null,
    val url: String? = null,
    val isFavorite: Boolean? = null
)

@Dao
interface HighlightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlights(highlights: List<HighlightEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlightTagRefs(refs: List<HighlightTagRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("SELECT * FROM highlights WHERE bookmarkId = :bookmarkId")
    suspend fun getHighlightsForBookmark(bookmarkId: String): List<HighlightEntity>

    @Transaction
    @Query("SELECT * FROM highlights WHERE _id = :highlightId")
    suspend fun getHighlightWithBookmark(highlightId: String): HighlightWithBookmark?

    @Transaction
    @Query("SELECT * FROM highlights WHERE bookmarkId = :bookmarkId")
    suspend fun getHighlightsWithBookmarkForBookmark(bookmarkId: String): List<HighlightWithBookmark>
}
