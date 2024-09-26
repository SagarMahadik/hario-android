package com.example.shared.state

import com.example.shared.state.AppStore.store

enum class ActionType {
    setFavorite,
    // Add more actions as needed
}

class ActionHandler {

    fun handleAction(action: ActionType, payload: Any) {
        when (action) {
            ActionType.setFavorite -> setFavorite(payload)
            // Add more actions as needed
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