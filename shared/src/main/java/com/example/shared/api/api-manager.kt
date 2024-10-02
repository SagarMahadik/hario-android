package com.example.shared.api

import android.util.Log
import com.example.shared.model.Collection
import com.example.shared.model.Tag
import com.example.shared.model.Bookmarks
import com.example.shared.model.Highlight
import com.example.shared.db.DbManager
import com.example.shared.db.toMap
import com.example.shared.db.MutationPayload
import com.example.shared.model.Settings
import com.example.shared.model.User
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import okhttp3.ResponseBody
import okio.IOException
import org.json.JSONObject
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

data class LoginPayload(
    val data: Map<String, Any?>,
    val operation: String
)

data class ApiResponse(
    val error: Any?,
    val data: Any?,
    val status: String?
)

data class StreamResponse(
    val type: String,
    val data: Any
)

data class FullBootstrapResponse(
    @SerializedName("error") val error: String?,
    @SerializedName("data") val data: BootstrapData
)

data class BootstrapData(
    @SerializedName("collections") val collections: List<Collection>,
    @SerializedName("tags") val tags: List<Tag>
)

sealed class FullBootstrapResult {
    data class Success(val data: FullBootstrapResponse) : FullBootstrapResult()
    data class Error(val exception: Exception) : FullBootstrapResult()
}

data class DeltaSyncPayload(
    val data: DeltaSyncRequestData
)

data class DeltaSyncRequestData(
    val fromSyncId: Int,
    val toSyncId: Int
)

data class DeltaSyncResponse(
    val data: DeltaSyncData
)

data class DeltaSyncData(
    val count: Int,
    val syncRecords: List<SyncRecord>
)

data class SyncRecord(
    val data: String,
    val collection: String,
    val operation: String,
    val userId: String,
    val updatedAt: String,
    val syncId: Int,
    val _id: String,
    val _ver: Int
)


class ApiManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: ApiManager? = null

        fun getInstance(): ApiManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ApiManager()
                INSTANCE = instance
                instance
            }
        }
    }

    private val apiService = ApiClient.apiService

    val dbManager = DbManager.getInstance()

    private fun setSessionId(sessionId: String) {
        ApiClient.sessionId = sessionId
    }

    suspend fun mutate(payload: MutationPayload): ApiResponse = withContext(Dispatchers.IO) {
        val response = apiService.mutate(payload)
        if (response.isSuccessful) {
            // Assuming the sessionId is returned in the response body
            val apiResponse = response.body()
            return@withContext apiResponse ?: ApiResponse(null, null, null)
        } else {
            throw Exception("API Error: ${response.errorBody()?.string()}")
        }
    }

    sealed class LoginResult<out T> {
        data class Success<out T>(val data: T) : LoginResult<T>()
        data class Error(val exception: Exception) : LoginResult<Nothing>()
    }

    suspend fun login(payload: LoginPayload): LoginResult<ApiResponse> =
        withContext(Dispatchers.IO) {

            val response = apiService.login(payload)
            if (response.isSuccessful) {
                // Assuming the sessionId is returned in the response body
                val apiResponse = response.body()

                Log.d("Debug", "login successful response")
                Log.d("Debug", "response: $apiResponse")

                if (payload.operation == "verifyEmailBasedLogin") {
                    Log.d("Debug", "verifyEmailBasedLogin operation")

                    val sessionId = apiResponse?.data?.let { data ->
                        Log.d("Debug", "Data: $data")
                        // Extract sessionId from data
                        when (data) {
                            is Map<*, *> -> data["sessionId"] as? String
                            else -> null
                        }
                    }

                    sessionId?.let {
                        Log.d("Debug", "Session ID: $it")
                        // Store sessionId using the global SessionManager instance
                        SessionManager.getInstance().setSessionId(it)
                        Log.d("Debug", "Session ID stored securely")
                    } ?: run {
                        Log.e("Error", "Session ID not found in response")
                    }
                }

                return@withContext apiResponse?.let { LoginResult.Success(it) }
                    ?: LoginResult.Error(Exception("Received null response"))

            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Error", "API Error: $errorMessage")
                return@withContext LoginResult.Error(Exception("API Error: $errorMessage"))
            }
        }

    data class GetUserResponse(
        val error: String?,
        val data: UserData?,
        val status: String?
    )

    data class UserData(
        val profile: User,
        val settings: Settings,
        val syncId: Int
    )

    sealed class GetUserResult {
        data class Success(val data: GetUserResponse) : GetUserResult()
        data class Error(val exception: Exception) : GetUserResult()
    }

    suspend fun getUser(): GetUserResult = withContext(Dispatchers.IO) {
        Log.d("Debug", "getUser function called")
        val response = apiService.getUser()
        if (response.isSuccessful) {
            response.body()?.let {
                Log.d("Debug", "getUser successful response")
                GetUserResult.Success(it)
            } ?: GetUserResult.Error(Exception("Received null response"))
        } else {
            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
            Log.e("Error", "API Error: $errorMessage")
            GetUserResult.Error(Exception("API Error: $errorMessage"))
        }
    }

    suspend fun fullBootstrap(): FullBootstrapResult = withContext(Dispatchers.IO) {
        try {
            Log.d("Debug", "fullBootstrap function called")
            val response = apiService.fullBootstrap()
            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("Debug", "fullBootstrap successful response")
                    FullBootstrapResult.Success(it)
                } ?: FullBootstrapResult.Error(Exception("Received null response"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Error", "API Error: $errorMessage")
                FullBootstrapResult.Error(Exception("API Error: $errorMessage"))
            }
        } catch (e: Exception) {
            Log.e("Error", "Exception in fullBootstrap", e)
            FullBootstrapResult.Error(e)
        }
    }

    suspend fun streamData() = withContext(Dispatchers.IO) {
        println("Stream reading started")
        val response = apiService.streamData()

        if (!response.isSuccessful) {
            throw Exception("API Error: ${response.errorBody()?.string()}")
        }

        val body: ResponseBody = response.body() ?: throw Exception("Empty response body")
        val reader = BufferedReader(InputStreamReader(body.byteStream()))

        var line: String?
        val bookmarksToInsert = mutableListOf<Bookmarks>()
        val highlightsToInsert = mutableListOf<Highlight>()

        while (reader.readLine().also { line = it } != null) {
            if (line?.isNotBlank() == true) {
                try {
                    val data = JSONObject(line)
                    when (data.getString("type")) {
                        "bookmarks" -> {
                            val bookmarkJson = data.getJSONObject("data")
                            val bookmark = parseBookmark(bookmarkJson)
                            bookmarksToInsert.add(bookmark)

                            // If we've accumulated a batch of bookmarks, insert them
                            if (bookmarksToInsert.size >= 500) {  // Adjust batch size as needed
                                dbManager.bookmarkRepository.insertBookmarks(bookmarksToInsert)
                                bookmarksToInsert.clear()
                            }
                        }

                        "highlights" -> {
                            val highlight = data.getJSONObject("data")
                            highlightsToInsert.add(Highlight(
                                _id = highlight.getString("_id"),
                                bookmarkId = highlight.getString("bookmarkId"),
                                isFavorite = highlight.optBoolean("isFavorite", false),
                                isSticky = highlight.optBoolean("isSticky", false),
                                color = highlight.optString("color", ""),
                                tags = highlight.optJSONArray("tags")?.let { array ->
                                    List(array.length()) { i -> array.getString(i) }
                                } ?: emptyList()
                            ))

                            // If we've accumulated a batch of highlights, insert them
                            if (highlightsToInsert.size >= 5) {  // Adjust batch size as needed
                                dbManager.highlightRepository.insertHighlights(highlightsToInsert)
                                highlightsToInsert.clear()
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error parsing JSON: $e, Line: $line")
                }
            }
        }

        println("Stream reading completed")
    }

    private fun parseBookmark(json: JSONObject): Bookmarks {
        return Bookmarks(
            _id = json.getString("_id"),
            title = json.getString("title"),
            url = json.getString("url"),
            isFavorite = json.optBoolean("isFavorite", false),
            tags = json.optJSONArray("tags")?.let { array ->
                List(array.length()) { i -> array.getString(i) }
            } ?: emptyList()
        )
    }

    sealed class DeltaSyncResult {
        data class Success(val data: DeltaSyncResponse) : DeltaSyncResult()
        data class Error(val exception: Exception) : DeltaSyncResult()
    }

    suspend fun deltaSync(fromSyncId: Int, toSyncId: Int): DeltaSyncResult =
        withContext(Dispatchers.IO) {
            try {
                Log.d(
                    "Debug",
                    "deltaSync function called with fromSyncId: $fromSyncId, toSyncId: $toSyncId"
                )

                val requestData = DeltaSyncRequestData(fromSyncId, toSyncId)
                val payload = DeltaSyncPayload(requestData)
                val response = apiService.deltaSync(payload)

                if (response.isSuccessful) {
                    response.body()?.let { deltaSyncResponse ->
                        Log.d("Debug", "deltaSync successful response")
                        DeltaSyncResult.Success(deltaSyncResponse)
                    } ?: DeltaSyncResult.Error(Exception("Received null response"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("Error", "API Error: $errorMessage")
                    DeltaSyncResult.Error(Exception("API Error: $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception during deltaSync: ${e.message}")
                DeltaSyncResult.Error(e)
            }
        }

    private var eventStreamStatus: String = "disconnected"
    private lateinit var clientId: String

    fun initEventStream(): Flow<String> = flow {
        var retryDelay = 1.seconds
        val maxDelay = 60.seconds

        while (true) {
            try {
                println("Initializing event stream")
                emit("Connecting to event stream...")

                clientId = generateUniqueClientId()
                val response = apiService.getEventStream(clientId)
                val responseBody = response.body()

                if (responseBody != null) {
                    eventStreamStatus = "connected"
                    emit("Event stream connected")
                    retryDelay = 1.seconds // Reset retry delay on successful connection

                    responseBody.byteStream().bufferedReader().use { reader ->
                        while (true) {
                            val line = reader.readLine()
                            if (line == null) {
                                println("End of stream reached")
                                break
                            }
                            if (line.startsWith("data:")) {
                                val jsonData = line.substring(5).trim()
                                processEventData(jsonData)
                            }
                        }
                    }
                } else {
                    throw IllegalStateException("Response body is null")
                }
            } catch (e: Exception) {
                println("SSE error: ${e.message}")
                eventStreamStatus = "disconnected"
                emit("Event stream disconnected: ${e.message}")

                // Implement exponential backoff
                delay(retryDelay)
                retryDelay = (retryDelay * 2).coerceAtMost(maxDelay)
                emit("Retrying connection in ${retryDelay.inWholeSeconds} seconds...")
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun processEventData(jsonData: String) {
        val message = JSONObject(jsonData)
        Log.d("SSE", "message is ${message}")

        if (message.optString("type") == "welcome") {
            Log.d("SSE", "set up event stream")
            eventStreamStatus = "connected"
            return
        }

        if (message.optString("clientId") != clientId) {
            val collection = message.optString("collection")
            val operation = message.optString("operation")
            val data = message.optJSONObject("data")
            val syncId = message.optString("syncId")
            val arrayOperation = data?.optString("arrayOperation")

            try {
                val mutationPayload = MutationPayload(
                    operation = operation,
                    arrayOperation = arrayOperation,
                    collection = collection,
                    data = data?.toMap() ?: emptyMap()
                )

                // Call mutate function from dbManager
                dbManager.mutate(mutationPayload)
                dbManager.setSyncId(syncId.toInt())
                println("Changes applied and syncId updated to $syncId")
            } catch (error: Exception) {
                println("Error applying changes: ${error.message}")
            }
        } else {
            println("Ignoring changes from own client")
        }
    }

    private fun generateUniqueClientId(): String {
        return UUID.randomUUID().toString()
    }
}

