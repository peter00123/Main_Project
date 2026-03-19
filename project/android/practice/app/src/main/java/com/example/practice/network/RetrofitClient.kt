package com.example.practice.network


import com.example.practice.WebURL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




object RetrofitClient {

    private const val BASE_URL = WebURL.message



    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
