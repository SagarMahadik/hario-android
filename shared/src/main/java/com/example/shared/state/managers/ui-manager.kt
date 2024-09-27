package com.example.shared.state.managers

import com.example.shared.db.MutationPayload
import com.example.shared.state.AppAction
import com.example.shared.state.AppStore
import com.example.shared.state.ItemType
import kotlinx.coroutines.runBlocking

object UiManager {
    private val dataManager = DataManager

    fun setFavorite(payload: Any) {
        if (payload is Map<*, *>) {
            val index = payload["index"] as? Int
            val itemId = payload["_id"] as? String
            val itemType = payload["itemType"] as? ItemType
            val value = payload["value"] as? Boolean

            if (index != null && itemId != null && itemType != null && value != null) {
                val dataToUpdate = mapOf("isFavorite" to value)

                // Create a MutationPayload
                val payload = MutationPayload(
                    operation = "add",
                    collection = "bookmarks",
                    data = mapOf(
                        "_id" to "12",
                        "title" to "sgrmhdk12",
                        "url" to "https://example.com",
                        "isFavorite" to false
                    )
                )

                // Use dataManager.mutate
                runBlocking {
                    try {
                        dataManager.mutate(payload)
                        // Optionally dispatch a success action
                        // AppStore.store.dispatch(AppAction.MutationSuccess(mutationPayload))
                    } catch (e: Exception) {
                        AppStore.store.dispatch(AppAction.ErrorOccurred("Mutation failed: ${e.message}"))
                    }
                }
            } else {
                AppStore.store.dispatch(AppAction.ErrorOccurred("Invalid payload for setFavorite"))
            }
        } else {
            AppStore.store.dispatch(AppAction.ErrorOccurred("Payload must be a map for setFavorite"))
        }
    }
}
