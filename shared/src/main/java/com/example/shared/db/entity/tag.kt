package com.example.shared.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    @ColumnInfo(name = "_id") // Though not necessary as Room uses variable name by default
    val _id: String,

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "parent")
    val parent: String,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date? = null,

    @ColumnInfo(name = "userId")
    val userId: String
)
