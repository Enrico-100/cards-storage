package com.example.cards_app.models

/**
 * Represents the supported types of verification.
 * The value will be serialized as a lowercase string (e.g., "email")
 * to match common API conventions.
 */
enum class VerificationType {
    EMAIL,
    PHONE
}