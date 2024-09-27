package com.example.shared.state.managers

import com.example.shared.api.ApiManager
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.state.AppAction
import com.example.shared.state.AppStore.store

object DataManager {
    private val dbManager: DbManager = DbManager.getInstance()
    private val apiManager = ApiManager.getInstance()

    suspend fun mutate(payload: MutationPayload) {
        try {
            dbManager.mutate(payload)
            // Optionally dispatch a success action
            // store.dispatch(AppAction.MutationSuccess(payload))
        } catch (e: Exception) {
            store.dispatch(AppAction.ErrorOccurred("Database mutation failed: ${e.message}"))
        }
    }
}