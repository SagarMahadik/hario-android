package com.example.shared.state.managers

import android.util.Log
import com.example.shared.api.ApiManager
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.state.AppAction
import com.example.shared.state.AppStore.store
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object DataManager {
    private const val TAG = "DataManager"
    private val dbManager: DbManager = DbManager.getInstance()
    private val apiManager = ApiManager.getInstance()

    suspend fun mutate(payload: MutationPayload) {
        try {
            coroutineScope {
                val dbJob = launch {
                    try {
                        dbManager.mutate(payload)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in DB mutation: ${e.message}", e)
                        throw e
                    }
                }
                val apiJob = launch {
                    try {
                        apiManager.mutate(payload)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in API mutation: ${e.message}", e)
                        throw e
                    }
                }

                // Wait for both jobs to complete
                dbJob.join()
                apiJob.join()
            }
            // Optionally dispatch a success action
            // store.dispatch(AppAction.MutationSuccess(payload))
            Log.d(TAG, "Mutation completed successfully for payload: $payload")
        } catch (e: Exception) {
            val errorMsg = "Mutation failed: ${e.message}"
            Log.e(TAG, errorMsg, e)
            store.dispatch(AppAction.ErrorOccurred(errorMsg))
        }
    }
}