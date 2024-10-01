package com.example.shared.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val _id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean,
    @ColumnInfo(name = "tags") val tags:String
)

data class BookmarkWithHighlights(
    @Embedded val bookmark: BookmarkEntity,
    @Relation(
        parentColumn = "_id",
        entityColumn = "bookmarkId"
    )
    val highlights: List<HighlightEntity>
)
