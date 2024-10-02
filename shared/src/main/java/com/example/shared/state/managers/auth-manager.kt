package com.example.shared.state.managers

import android.util.Log
import com.example.shared.api.ApiManager
import com.example.shared.api.FullBootstrapResult
import com.example.shared.db.DbManager
import com.example.shared.api.LoginPayload
import com.example.shared.model.Collection
import com.example.shared.model.Settings
import com.example.shared.model.Tag
import com.example.shared.model.User
import com.example.shared.state.AppAction
import com.example.shared.state.AppStore.store
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object AuthManager {
    private val apiManager = ApiManager.getInstance()
    private val dbManager = DbManager.getInstance()

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

    private suspend fun loadCoreData() {
       coroutineScope {
           launch {
               dbManager.getCollectionsFlow().collectLatest {
                   collections: List<Collection> ->
                   Log.d("Debug", "Collections loaded in coreData")
                   store.dispatch(AppAction.SetCollections(collections))
               }
           }
           launch{
                dbManager.getTagsFlow().collectLatest {
                     tags: List<Tag> ->
                     Log.d("Debug", "Tags loaded in coreData")
                     store.dispatch(AppAction.SetTags(tags))
                }
           }

           launch {
               dbManager.getSettings().collectLatest { settings: Settings? ->
                   Log.d("Debug", "Settings loaded in coreData: $settings")
                  settings?.let { store.dispatch(AppAction.SetSettings(it)) }
               }
           }

           launch {
               dbManager.getUser().collectLatest { user: User? ->
                   Log.d("Debug", "User loaded in coreData: $user")
                  user?.let { store.dispatch(AppAction.SetUser(it)) }
               }
           }
             Log.d("Debug", "loadCoreData completed")
       }
    }

    private suspend fun fullBootstrap() {
        when (val fullBootstrapResult = apiManager.fullBootstrap()) {
            is FullBootstrapResult.Success -> {
                val bootstrapResponse = fullBootstrapResult.data
                Log.d("Debug", "fullBootstrap successful. Response: $bootstrapResponse")

                val collections = bootstrapResponse.data.collections.map { collectionData ->
                    Collection(
                        _id = collectionData._id,
                        name = collectionData.name,
                        parent = collectionData.parent,
                        updatedAt = collectionData.updatedAt,
                        userId = collectionData.userId,
                        isFavorite = collectionData.isFavorite ?: false
                    )
                }

                // Insert or update collections in the database
                dbManager.collectionRepository.insertCollections(collections)

                // Process tags
                val tags = bootstrapResponse.data.tags.map { tagData ->
                    Tag(
                        _id = tagData._id,
                        name = tagData.name,
                        updatedAt = tagData.updatedAt,
                        userId = tagData.userId,
                        isFavorite = tagData.isFavorite,
                        parent = tagData.parent
                    )
                }

                // Insert or update tags in the database
                dbManager.tagRepository.insertTags(tags)

                try {
                    coroutineScope {
                        launch {
                            loadCoreData()
                        }
                        launch {
                            apiManager.streamData()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Error in background tasks: ${e.message}", e)
                    // Handle the error as needed
                }

                // Add any other necessary processing for the bootstrap data
                Log.d("Debug", "Bootstrap data processed successfully")
            }
            is FullBootstrapResult.Error -> {
                val error = fullBootstrapResult.exception
                Log.e("Error", "fullBootstrap error: ${error.message}", error)
                // Handle bootstrap error here
                // You might want to retry the operation or inform the user
            }
        }
    }

    private suspend fun deltaSync(lastSyncId: Int, currentSyncId: Int) {
        loadCoreData()
        when (val result = apiManager.deltaSync(lastSyncId, currentSyncId)) {
            is ApiManager.DeltaSyncResult.Success -> {
                val deltaSyncResponse = result.data
                Log.d("delta-sync", "Delta sync response: $deltaSyncResponse")
                dbManager.applyDeltaChanges(deltaSyncResponse.data.syncRecords)
            }
            is ApiManager.DeltaSyncResult.Error -> {
                Log.e("Error", "Delta sync failed: ${result.exception.message}")
                // Handle sync error
            }
        }
    }

    suspend fun startUpSequence(payload: Any) {
        Log.d("Debug", "startUpSequence called")
        val getUserResult = apiManager.getUser()
        when (getUserResult) {
            is ApiManager.GetUserResult.Success -> {
                val userResponse = getUserResult.data
                Log.d("Debug", "getUser successful. Response: $userResponse")

                val apiProfile = userResponse.data?.profile
                val apiSettings = userResponse.data?.settings

                // Save user profile to local database
                apiProfile?.let { profile ->
                    val user = User(
                        _id = profile._id,
                        email = profile.email,
                        userId = profile.userId
                    )
                    dbManager.saveUser(user)
                    Log.d("Debug", "User saved to local database")
                }

                // Save settings to local database
                apiSettings?.let { settings ->
                    val localSettings = Settings(
                        _id = settings._id,
                        dock = Settings.Dock(size = settings.dock.size),
                        sidebar = Settings.Sidebar(
                            position = settings.sidebar.position,
                            size = settings.sidebar.size
                        ),
                        userId = settings.userId
                    )
                    dbManager.saveSettings(localSettings)
                    Log.d("Debug", "Settings saved to local database")
                }

                val syncIdInServer = userResponse.data?.syncId
                val syncIdInLocalDb = dbManager.getSyncId()

                Log.d("SYNCID","syncId in localDB is : ${syncIdInLocalDb}")

                val fullBootstrapResult = apiManager.fullBootstrap()
                when (fullBootstrapResult) {
                    is FullBootstrapResult.Success -> {
                        val bootstrapResponse = fullBootstrapResult.data
                        Log.d("Debug", "fullBootstrap successful. Response: $bootstrapResponse")

                        // Extract collections from the bootstrap response
                        val collections = bootstrapResponse.data.collections.map { collectionData ->
                            Collection(
                                _id = collectionData._id,
                                name = collectionData.name,
                                parent = collectionData.parent,
                                updatedAt = collectionData.updatedAt,
                                userId = collectionData.userId,
                                isFavorite = collectionData.isFavorite ?: false
                            )
                        }

                        // Insert or update collections in the database
                        dbManager.collectionRepository.insertCollections(collections)

                        // Process tags
                        val tags = bootstrapResponse.data.tags.map { tagData ->
                            Tag(
                                _id = tagData._id,
                                name = tagData.name,
                                updatedAt = tagData.updatedAt,
                                userId = tagData.userId,
                                isFavorite = tagData.isFavorite,
                                parent = tagData.parent
                            )
                        }

                        // Insert or update tags in the database
                        dbManager.tagRepository.insertTags(tags)

                        // Add any other necessary processing for the bootstrap data
                        Log.d("Debug", "Bootstrap data processed successfully")

                        try {
                            coroutineScope {
                                launch {
                                    loadCoreData()
                                }
                                launch {
//                                    apiManager.streamData()

                                    val lastSyncId = 467 // Get this from your local storage
                                    val currentSyncId = 468 // This should be obtained from your server or business logic

                                    when (val result = apiManager.deltaSync(lastSyncId, currentSyncId)) {
                                        is ApiManager.DeltaSyncResult.Success -> {
                                            val deltaSyncResponse = result.data
                                            // Process the data here in your business logic
                                            Log.d("delta-sync","Delta sync response:${deltaSyncResponse}")
                                             dbManager.applyDeltaChanges(deltaSyncResponse.data.syncRecords)
                                        }
                                        is ApiManager.DeltaSyncResult.Error -> {
                                            Log.e("Error", "Delta sync failed: ${result.exception.message}")
                                            // Handle sync error
                                        }
                                    }
                                    apiManager.initEventStream().collectLatest {
                                        status -> print(status)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Error", "Error loading core data: ${e.message}", e)
                            // Handle the error as needed
                        }
                    }
                    is FullBootstrapResult.Error -> {
                        val error = fullBootstrapResult.exception
                        Log.e("Error", "fullBootstrap error: ${error.message}", error)
                        // Handle bootstrap error here
                        // You might want to retry the operation or inform the user
                    }
                }
            }
            is ApiManager.GetUserResult.Error -> {
                val error = getUserResult.exception
                Log.e("Error", "getUser error: ${error.message}", error)
                store.dispatch(AppAction.LoginRequired)
            }
        }
    }
}
