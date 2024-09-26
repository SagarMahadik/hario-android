package com.example.shared.model

data class Collections(
    val _id: String,
    val title: String,
    val parent: String
) : UpdatableItem<Collections> {

    override fun update(data: Map<String, Any>): Collections {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "title" -> updated.copy(title = value as String)
                "parent" -> updated.copy(parent = value as String)
                else -> updated
            }
        }
        return updated
    }
}
