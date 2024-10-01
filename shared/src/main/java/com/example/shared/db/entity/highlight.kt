package com.example.shared.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = BookmarkEntity::class,
            parentColumns = ["_id"],
            childColumns = ["bookmarkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HighlightEntity(
    @PrimaryKey val _id: String,
    @ColumnInfo(name = "bookmarkId") val bookmarkId: String,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "isSticky") val isSticky: Boolean,
    @ColumnInfo(name = "tags") val tags: String
)

data class HighlightWithBookmark(
    @Embedded val highlight: HighlightEntity,
    @Relation(
        parentColumn = "bookmarkId",
        entityColumn = "_id"
    )
    val bookmark: BookmarkEntity
)