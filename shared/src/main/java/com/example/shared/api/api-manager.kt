package com.example.shared.api

// ApiManager.kt
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// MutationPayload.kt
data class MutationPayload(
    val operation: String, // "add", "update", or "delete"
    val collection: String,
    val data: Map<String, Any?>
)

data class LoginPayload(
    val data: Map<String, Any?>,
    val operation: String
)

// ApiResponse.kt
data class ApiResponse(
    val error: Any?,
    val data: Any?,
    val status: String?
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

    suspend fun login(payload: LoginPayload): LoginResult<ApiResponse> = withContext(Dispatchers.IO) {

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

    sealed class GetUserResult<out T> {
        data class Success<out T>(val data: T) : GetUserResult<T>()
        data class Error(val exception: Exception) : GetUserResult<Nothing>()
    }

    suspend fun getUser(): GetUserResult<ApiResponse> = withContext(Dispatchers.IO) {
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

    // Implement other methods like login, fullBootStrap, etc., as needed
}
