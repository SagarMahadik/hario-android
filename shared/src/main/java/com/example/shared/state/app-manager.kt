package com.example.shared.state

import android.util.Log
import com.example.shared.api.ApiManager
import com.example.shared.api.LoginPayload
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.state.AppStore.store
import kotlinx.serialization.Serializable

enum class ActionType {
    setFavorite,
    initiateEmailBasedLogin,
    verifyEmailBasedLogin
    // Add more actions as needed
}

class AppManager() {
    private val dbManager: DbManager = DbManager.getInstance()
    private val apiManager = ApiManager.getInstance()

    suspend fun handleAction(action: ActionType, payload: Any) {
        when (action) {
            ActionType.setFavorite -> setFavorite(payload)
            ActionType.initiateEmailBasedLogin -> initiateEmailBasedLogin(payload)
            ActionType.verifyEmailBasedLogin -> verifyEmailBasedLogin(payload)
        }
    }

    data class InitiateEmailLoginPayload(val email: String)

    private suspend fun initiateEmailBasedLogin(payload: Any){
        Log.d("Debug", "initiateEmailBasedLogin called with payload: $payload")
        if (payload is InitiateEmailLoginPayload) {
            
            val email = payload.email

            val data = mapOf(
                "email" to email,
                "clientAuthCode" to "123456"
            )

            val loginPayload = LoginPayload (
                data = data,
                operation = "initiateEmailBasedLogin"
            )

            apiManager.login(loginPayload)
           
        } else {
            Log.e("Error", "Invalid payload for initiateEmailBasedLogin")
            store.dispatch(
                AppAction.ErrorOccurred("Invalid payload for initiateEmailBasedLogin")
            )
        }
    }

    data class VerifyEmailLoginPayload(val email: String, val token: String)

    private suspend fun verifyEmailBasedLogin(payload: Any) {
        Log.d("Debug", "verifyEmailBasedLogin called with payload: $payload")
        if (payload is VerifyEmailLoginPayload) {
            // Prepare the data payload
            val data = mapOf(
                "email" to payload.email,
                "verificationToken" to payload.token
            )

            val loginPayload = LoginPayload(
                data = data,
                operation = "verifyEmailBasedLogin"
            )

            // Handle the API login result
            when (val result = apiManager.login(loginPayload)) {
                is ApiManager.LoginResult.Success  -> {
                    Log.d("Debug", "Login successful: ${result.data}")
                   store.dispatch(AppAction.LoginSuccess)
                }
                is ApiManager.LoginResult.Error -> {
                    Log.e("Error", "Login failed: ${result.exception.message}")
                    store.dispatch(
                        AppAction.ErrorOccurred("Login failed: ${result.exception.message}")
                    )
                }
            }
        } else {
            Log.e("Error", "Invalid payload for verifyEmailBasedLogin")
            store.dispatch(
                AppAction.ErrorOccurred("Invalid payload for verifyEmailBasedLogin")
            )
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

    suspend fun startUpSequence() {
        Log.d("Debug", "startUpSequence called")
        val result = apiManager.getUser()
        when (result) {
            is ApiManager.GetUserResult.Success -> {
                val response = result.data
                Log.d("Debug", "startUpSequence successful")
                // Handle the successful response
            }
            is ApiManager.GetUserResult.Error -> {
                Log.e("Error", "startUpSequence error: ${result.exception.message}")
                store.dispatch(
                    AppAction.LoginRequired
                )
            }
        }
    }
}