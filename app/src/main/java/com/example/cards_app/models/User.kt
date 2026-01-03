package com.example.cards_app.models

import com.example.cards_app.Card

// Represents the user object for creating a new user.
// Fields are nullable because not all are required for creation.
data class User(
    val id: Long? = null,
    val username: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val passwordHash: String? = null, // In Android, this will hold the plain-text password to be sent
    val name: String? = null,
    val cards: List<Card>? = null,
    val emailVerified: Boolean? = null,
    val phoneVerified: Boolean? = null
)

