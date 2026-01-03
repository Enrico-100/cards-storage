package com.example.cards_app.models

data class VerificationRequest(
    val code: String,
    val type: VerificationType
)

