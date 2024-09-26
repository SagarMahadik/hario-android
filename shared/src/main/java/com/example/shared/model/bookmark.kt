package com.example.shared.model

data class Bookmarks(
    val _id: String,
    val title: String,
    val url: String,
    val isFavorite: Boolean
) : UpdatableItem<Bookmarks> {

    override fun update(data: Map<String, Any>): Bookmarks {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "title" -> updated.copy(title = value as String)
                "url" -> updated.copy(url = value as String)
                "isFavorite" -> updated.copy(isFavorite = value as Boolean)
                else -> updated
            }
        }
        return updated
    }
}
