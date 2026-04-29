package com.example.prefinalproj.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitClient.kt
 * ------------------
 * This is a SINGLETON object — it creates ONE shared Retrofit instance
 * that the whole app reuses. Think of it like a shared "HTTP engine."
 *
 * Why a singleton? Creating Retrofit from scratch on every API call is
 * slow and wasteful. One instance, shared everywhere = efficient.
 */
object RetrofitClient {

    // Base URL for ALL your API endpoints.
    // Every API call will be appended to this URL.
    private const val BASE_URL = "http://hyeumine.com/"

    /**
     * Lazy means this code only runs the FIRST time `apiService` is accessed.
     * After that, the same instance is reused.
     */
    val apiService: ForumApiService by lazy {

        // --- Step 1: Set up logging (so you can see API traffic in Logcat) ---
        val logging = HttpLoggingInterceptor().apply {
            // BODY level prints the full request + response body — great for debugging
            level = HttpLoggingInterceptor.Level.BODY
        }

        // --- Step 2: Build the HTTP client with the logger attached ---
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // --- Step 3: Build Retrofit ---
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // GsonConverterFactory automatically converts JSON <-> Kotlin data classes
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            // Finally, create our API interface (defined in ForumApiService.kt)
            .create(ForumApiService::class.java)
    }
}