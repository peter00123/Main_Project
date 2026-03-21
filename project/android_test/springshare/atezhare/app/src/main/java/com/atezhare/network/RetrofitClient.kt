// network/RetrofitClient.kt
// Singleton Retrofit instance used by all network calls across the app.
// Uses OkHttp with logging interceptor for debugging.
// Depends on: ApiConstants (BASE_URL, timeouts), ApiService (interface)

package com.atezhare.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Lazy-initialized Retrofit instance
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            // Auth token interceptor — reads token from SessionManager if available
            .addInterceptor { chain ->
                val original = chain.request()
                // Token is added here if session is active; SessionManager is accessed statically
                val token = SessionTokenHolder.token
                val request = if (token != null) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Exposed API service — call RetrofitClient.apiService.someEndpoint()
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

// Holds the current auth token in memory — updated by utils/SessionManager on login
object SessionTokenHolder {
    var token: String? = null
}
