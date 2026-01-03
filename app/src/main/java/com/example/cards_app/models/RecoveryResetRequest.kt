package com.example.cards_app.models

data class RecoveryResetRequest(
    val username: String,
    val code: String,
    val newPassword: String
)
