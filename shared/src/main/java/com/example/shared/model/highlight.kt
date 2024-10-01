package com.example.shared.model

data class Highlight(
    val _id: String,
    val bookmarkId: String,
    val isFavorite: Boolean,
    val isSticky: Boolean = false,
    val color: String = "",
    val tags: List<String> = emptyList()
) : UpdatableItem<Highlight> {

    override fun update(data: Map<String, Any>): Highlight {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "isFavorite" -> updated.copy(isFavorite = value as Boolean)
                else -> updated
            }
        }
        return updated
    }
}
