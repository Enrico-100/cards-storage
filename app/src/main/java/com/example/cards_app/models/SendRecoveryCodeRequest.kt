package com.example.cards_app.models

data class SendRecoveryCodeRequest(
    val username: String,
    val channel: VerificationType
)
