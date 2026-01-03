package com.example.cards_app.models

import kotlinx.serialization.Serializable

@Serializable
data class RecoveryInitiateRequest(
    val username: String
)

