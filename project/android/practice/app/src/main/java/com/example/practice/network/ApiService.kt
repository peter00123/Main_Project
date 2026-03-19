package com.example.practice.network

import com.example.practice.archives.wifi.LoginRequest
import com.example.practice.archives.wifi.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


// for login
interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

}