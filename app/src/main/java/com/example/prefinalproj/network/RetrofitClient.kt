package com.example.prefinalproj.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://hyeumine.com/"


    val apiService: ForumApiService by lazy {


        val logging = HttpLoggingInterceptor().apply {

            level = HttpLoggingInterceptor.Level.BODY
        }


        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)

            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

            .create(ForumApiService::class.java)
    }
}