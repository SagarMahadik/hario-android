package com.example.shared.db.refs

import androidx.room.Entity
import androidx.room.ForeignKey

import com.example.shared.db.entity.HighlightEntity@Entity(tableName = "highlight_tag_ref",
    primaryKeys = ["highlightId", "tag"],
    foreignKeys = [ForeignKey(entity = HighlightEntity::class,
        parentColumns = ["_id"],
        childColumns = ["highlightId"],
        onDelete = ForeignKey.CASCADE)])

data class HighlightTagRef(
    val highlightId: String,
    val tag: String
)