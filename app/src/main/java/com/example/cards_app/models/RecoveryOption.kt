package com.example.cards_app.models

data class RecoveryOption(
    val channel: VerificationType, // "email" or "phone"
    val maskedValue: String,
)
