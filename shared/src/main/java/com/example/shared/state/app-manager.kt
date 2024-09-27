package com.example.shared.state

import com.example.shared.api.ApiManager
import com.example.shared.api.LoginPayload
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.state.AppStore.store
import com.example.shared.state.managers.AuthManager
import com.example.shared.state.managers.DataManager
import com.example.shared.state.managers.UiManager
import kotlinx.serialization.Serializable

enum class ActionType {
    setFavorite,
    InitiateEmailBasedLogin,
    VerifyEmailBasedLogin,
    StartUpSequence
    // Add more actions as needed
}

class AppManager() {
    private val authManager = AuthManager
    private val dataManager = DataManager
    private val uiManager = UiManager

    suspend fun handleAction(action: ActionType, payload: Any) {
        when (action) {
            ActionType.setFavorite -> uiManager.setFavorite(payload)
            ActionType.InitiateEmailBasedLogin -> authManager.initiateEmailBasedLogin(payload)
            ActionType.VerifyEmailBasedLogin -> authManager.verifyEmailBasedLogin(payload)
            ActionType.StartUpSequence -> authManager.startUpSequence(payload)
        }
    }
}