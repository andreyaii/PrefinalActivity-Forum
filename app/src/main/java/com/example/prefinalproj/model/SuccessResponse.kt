package com.example.prefinalproj.model

import com.google.gson.annotations.SerializedName

/**
 * SuccessResponse.kt
 * -------------------
 * A simple wrapper for API responses that just return { "success": true/false }.
 * Used for: createPost, deletePost, replyToPost, deleteReply.
 */
data class SuccessResponse(
    @SerializedName("success")
    val success: Boolean
)