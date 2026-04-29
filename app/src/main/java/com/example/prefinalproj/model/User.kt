package com.example.prefinalproj.model

import com.google.gson.annotations.SerializedName

/**
 * User.kt
 * --------
 * Represents a user returned from the login or register API.
 *
 * @SerializedName("id") tells Gson: "when you see 'id' in JSON, put it in this field."
 * This is needed if your JSON key name doesn't match the Kotlin property name.
 *
 * Example JSON: { "id": 5, "username": "alice" }
 */
data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String
)
