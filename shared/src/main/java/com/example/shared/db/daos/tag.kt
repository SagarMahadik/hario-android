package com.example.shared.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.shared.db.entity.BookmarkEntity
import com.example.shared.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

data class PartialTagEntity(
    @PrimaryKey val _id: String,
    val title: String? = null,
    val url: String? = null,
    val isFavorite: Boolean? = null
)

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("SELECT * FROM tags WHERE _id = :id")
    suspend fun getTagById(id: String): TagEntity?

    @Update
    suspend fun updateTag(tag: TagEntity)
}