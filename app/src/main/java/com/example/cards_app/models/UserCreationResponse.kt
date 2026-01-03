package com.example.cards_app.models

// Represents the successful response from the POST /api/users endpoint.
data class UserCreationResponse(
    val message: String,
    val userId: Long,
    val username: String,
    val name: String?
)

