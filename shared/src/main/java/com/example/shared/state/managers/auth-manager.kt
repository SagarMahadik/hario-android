package com.example.shared.state.managers

import android.util.Log
import com.example.shared.api.ApiManager
import com.example.shared.api.LoginPayload
import com.example.shared.state.AppAction
import com.example.shared.state.AppStore.store

object AuthManager {
    private val apiManager = ApiManager.getInstance()

    data class InitiateEmailLoginPayload(val email: String)
    suspend fun initiateEmailBasedLogin(payload: Any) {
        if (payload is InitiateEmailLoginPayload) {
            val email = payload.email
            val data = mapOf(
                "email" to email,
                "clientAuthCode" to "123456"
            )
            val loginPayload = LoginPayload(
                data = data,
                operation = "initiateEmailBasedLogin"
            )
            apiManager.login(loginPayload)
        } else {
            Log.e("Error", "Invalid payload for initiateEmailBasedLogin")
            store.dispatch(AppAction.ErrorOccurred("Invalid payload for initiateEmailBasedLogin"))
        }
    }

    data class VerifyEmailLoginPayload(val email: String, val token: String)

    suspend fun verifyEmailBasedLogin(payload: Any) {
        if (payload is VerifyEmailLoginPayload) {
            val data = mapOf(
                "email" to payload.email,
                "verificationToken" to payload.token
            )
            val loginPayload = LoginPayload(
                data = data,
                operation = "verifyEmailBasedLogin"
            )
            when (val result = apiManager.login(loginPayload)) {
                is ApiManager.LoginResult.Success -> {
                    Log.d("Debug", "Login successful: ${result.data}")
                    store.dispatch(AppAction.LoginSuccess)
                }
                is ApiManager.LoginResult.Error -> {
                    Log.e("Error", "Login failed: ${result.exception.message}")
                    store.dispatch(AppAction.ErrorOccurred("Login failed: ${result.exception.message}"))
                }
            }
        } else {
            Log.e("Error", "Invalid payload for verifyEmailBasedLogin")
            store.dispatch(AppAction.ErrorOccurred("Invalid payload for verifyEmailBasedLogin"))
        }
    }

    suspend fun startUpSequence(payload: Any) {
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
                store.dispatch(AppAction.LoginRequired)
            }
        }
    }
}
