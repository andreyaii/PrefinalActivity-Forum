package com.example.prefinalproj.model

import com.google.gson.annotations.SerializedName

/**
 * Post.kt
 * --------
 * Represents a forum post, which may contain a list of replies.
 *
 * Example JSON:
 * {
 *   "id": 12,
 *   "user_id": 5,
 *   "username": "alice",
 *   "post": "Hello world!",
 *   "replies": [ { ... }, { ... } ]
 * }
 */
data class Post(
    @SerializedName("id")
    val id: Int,

    @SerializedName("uid")
    val userId: Int,

    @SerializedName("user")
    val username: String,

    @SerializedName("post")
    val postText: String,

    // "replies" is a nested list inside the Post JSON object.
    // If the API returns null here, we default to an empty list.
    @SerializedName("replies")
    val replies: List<Reply>? = emptyList()
)

