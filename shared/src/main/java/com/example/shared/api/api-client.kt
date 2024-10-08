package com.example.shared.api
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    var sessionId: String? = null

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(SessionInterceptor())
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl("https://cherrypic-in-uat-api.fly.dev")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    // Custom Interceptor Class
    class SessionInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("X-App-Source", "MOBILE-ANDROID")

            SessionManager.getInstance().getSessionId()?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }

            return chain.proceed(requestBuilder.build())
        }
    }
}
