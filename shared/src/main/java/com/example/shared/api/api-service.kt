package com.example.shared.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/mutate")
    suspend fun mutate(@Body payload: MutationPayload): Response<ApiResponse>

    @POST("/login")
    suspend fun login(@Body payload: LoginPayload): Response<ApiResponse>

    @GET("/get-user")
    suspend fun getUser(): Response<ApiResponse>

    // Define other endpoints like login, fullBootStrap, etc., as needed
}
