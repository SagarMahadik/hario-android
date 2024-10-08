package com.example.shared.api

import com.example.shared.db.MutationPayload
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @POST("/mutate")
    suspend fun mutate(@Body payload: MutationPayload): Response<ApiResponse>

    @POST("/login")
    suspend fun login(@Body payload: LoginPayload): Response<ApiResponse>

    @GET("/get-user")
    suspend fun getUser(): Response<ApiManager.GetUserResponse>

    @GET("/full-bootstrap")
    suspend fun fullBootstrap(): Response<FullBootstrapResponse>

    @GET("/stream-data")
    suspend fun streamData(): Response<ResponseBody>

    @POST("/delta-sync")
    suspend fun deltaSync(@Body payload: DeltaSyncPayload): Response<DeltaSyncResponse>

    @GET("/event-stream")
    @Streaming
    suspend fun getEventStream(@Query("clientId") clientId: String): Response<ResponseBody>
}
