package com.example.prefinalproj.model

import com.google.gson.annotations.SerializedName

/**
 * Reply.kt
 * ---------
 * Represents a single reply on a post.
 *
 * Example JSON:
 * {
 *   "id": 3,
 *   "user_id": 5,
 *   "post_id": 12,
 *   "username": "alice",
 *   "reply": "Great post!"
 * }
 */
data class Reply(
    @SerializedName("id")
    val id: Int,

    @SerializedName("uid")
    val userId: Int,

    @SerializedName("post_id")
    val postId: Int,

    @SerializedName("user")
    val username: String,

    @SerializedName("reply")
    val replyText: String
)
