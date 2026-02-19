package com.example.practice.network

import com.example.practice.LoginRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<String>

    @GET("api/message")
    fun getMessage(): Call<String>
}
