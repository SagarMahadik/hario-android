package com.example.shared.db.refs

import androidx.room.Entity
import androidx.room.ForeignKey

import com.example.shared.db.entity.BookmarkEntity@Entity(tableName = "bookmark_tag_ref",
    primaryKeys = ["bookmarkId", "tag"],
    foreignKeys = [ForeignKey(entity = BookmarkEntity::class,
        parentColumns = ["_id"],
        childColumns = ["bookmarkId"],
        onDelete = ForeignKey.CASCADE)])

data class BookmarkTagRef(
    val bookmarkId: String,
    val tag: String
)