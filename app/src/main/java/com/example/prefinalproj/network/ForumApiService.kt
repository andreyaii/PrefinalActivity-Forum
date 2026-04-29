package com.example.prefinalproj.network

import com.example.prefinalproj.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * ForumApiService.kt
 * -------------------
 * This is a Retrofit "interface" — it defines WHAT API calls are available.
 * Retrofit reads these function signatures and builds the actual HTTP code for you.
 *
 * Key annotations explained:
 *   @GET("path")      → Makes an HTTP GET request to BASE_URL + "path"
 *   @POST("path")     → Makes an HTTP POST request
 *   @Query("key")     → Adds ?key=value to the URL
 *   @Field("key")     → Adds key=value in the POST request body (form data)
 *   @FormUrlEncoded   → Required when using @Field — sends data as a form
 *   suspend           → Marks function as a Kotlin Coroutine (runs off the main thread)
 */
interface ForumApiService {

    /**
     * 1. GET POSTS (paginated)
     * URL: http://hyeumine.com/forumGetPosts.php?page=1
     * Returns a list of Post objects.
     */
    @GET("forumGetPosts.php")
    suspend fun getPosts(
        @Query("page") page: Int  // ?page=1, ?page=2, etc.
    ): Response<List<Post>>

    /**
     * 2. REGISTER USER
     * URL: http://hyeumine.com/forumCreateUser.php
     * Sends username + password as form fields.
     * Returns a User object if successful.
     */
    @FormUrlEncoded
    @POST("forumCreateUser.php")
    suspend fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<User>

    /**
     * 3. LOGIN
     * URL: http://hyeumine.com/forumLogin.php
     * Returns a User object if credentials match, or null/false if not.
     *
     * NOTE: The API returns "false" (a string) on failure.
     * We use Response<User> and check isSuccessful + body != null.
     */
    @FormUrlEncoded
    @POST("forumLogin.php")
    suspend fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<User>

    /**
     * 4. CREATE POST
     * URL: http://hyeumine.com/forumNewPost.php
     * Sends the user's ID and post text.
     */
    @FormUrlEncoded
    @POST("forumNewPost.php")
    suspend fun createPost(
        @Field("id") userId: Int,
        @Field("post") postText: String
    ): Response<SuccessResponse>

    /**
     * 5. DELETE POST
     * URL: http://hyeumine.com/forumDeletePost.php?id=42
     * Deletes the post with the given post ID.
     */
    @GET("forumDeletePost.php")
    suspend fun deletePost(
        @Query("id") postId: Int
    ): Response<SuccessResponse>

    /**
     * 6. REPLY TO POST
     * URL: http://hyeumine.com/forumReplyPost.php
     * Sends user_id, post_id, and reply text.
     */
    @FormUrlEncoded
    @POST("forumReplyPost.php")
    suspend fun replyToPost(
        @Field("user_id") userId: Int,
        @Field("post_id") postId: Int,
        @Field("reply") replyText: String
    ): Response<SuccessResponse>

    /**
     * 7. DELETE REPLY
     * URL: http://hyeumine.com/forumDeleteReply.php?id=7
     * Deletes the reply with the given reply ID.
     */
    @GET("forumDeleteReply.php")
    suspend fun deleteReply(
        @Query("id") replyId: Int
    ): Response<SuccessResponse>
}