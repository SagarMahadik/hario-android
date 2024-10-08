package com.example.shared.model

import java.util.Date

data class Collection(
    val _id: String,
    val name: String,
    val parent: String,
    val updatedAt: Date?,
    val userId: String,
    val isFavorite: Boolean?,
) : UpdatableItem<Collection> {

    override fun update(data: Map<String, Any>): Collection {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "name" -> updated.copy(name = value as String)
                "parent" -> updated.copy(parent = value as String)
                "updatedAt" -> updated.copy(updatedAt = value as Date)
                "userId" -> updated.copy(userId = value as String)
                "isFavorite" -> updated.copy(isFavorite = value as Boolean)
                else -> updated
            }
        }
        return updated
    }
}
