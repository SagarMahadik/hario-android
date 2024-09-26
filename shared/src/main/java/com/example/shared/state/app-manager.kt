package com.example.shared.state

import com.example.shared.db.AppDatabase
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.state.AppStore.store

enum class ActionType {
    setFavorite,
    // Add more actions as needed
}

class ActionHandler() {
    private val dbManager: DbManager = DbManager.getInstance()
    fun handleAction(action: ActionType, payload: Any) {
        when (action) {
            ActionType.setFavorite -> setFavorite(payload)
            // Add more actions as needed
        }
    }

     suspend fun mutate(payload: MutationPayload) {
        try {
            dbManager.mutate(payload)
            // Optionally dispatch a success action
            // store.dispatch(AppAction.MutationSuccess(payload))
        } catch (e: Exception) {
            // Dispatch an error action
            store.dispatch(AppAction.ErrorOccurred("Database mutation failed: ${e.message}"))
        }
    }

    private fun setFavorite(payload: Any) {
        if (payload is Map<*, *>) {
            val index = payload["index"] as? Int
            val itemId = payload["_id"] as? String
            val itemType = payload["itemType"] as? ItemType
            val value = payload["value"] as? Boolean

            if (index != null && itemId != null && itemType != null && value != null) {
                val dataToUpdate = mapOf("isFavorite" to value)

                store.dispatch(
                    AppAction.UpdateItem(
                        index = index,
                        itemId = itemId,
                        itemType = itemType,
                        data = dataToUpdate
                    )
                )
            } else {
                store.dispatch(
                    AppAction.ErrorOccurred("Invalid payload for setFavorite")
                )
            }
        } else {
            store.dispatch(
                AppAction.ErrorOccurred("Payload must be a map for setFavorite")
            )
        }
    }

}