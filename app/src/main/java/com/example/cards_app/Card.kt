package com.example.cards_app

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val id: String,
    val number: Long,
    val name: String,
    val nameOfCard: String,
    val picture: String?,
    val color: String
)
