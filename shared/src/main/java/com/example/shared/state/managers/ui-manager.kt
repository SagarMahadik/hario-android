package com.example.shared.state.managers

import android.util.Log
import com.example.shared.db.MutationPayload
import com.example.shared.state.AppAction
import com.example.shared.state.AppStore
import com.example.shared.state.ItemType
import com.example.shared.state.mapItemTypeToString
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
                val dataToUpdate = mapOf(
                    "_id" to itemId,
                    "isFavorite" to value
                )

                AppStore.store.dispatch(AppAction.UpdateItem(index, itemId, dataToUpdate, itemType))

                runBlocking {
                    try {
                        dataManager.mutate(
                            MutationPayload(
                                "update",
                                "",
                                mapItemTypeToString(itemType),
                                dataToUpdate
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("set_fav_error", "Error in setFavorite: ${e.message}", e)
                        AppStore.store.dispatch(AppAction.ErrorOccurred("Failed to set favorite: ${e.message}"))
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
