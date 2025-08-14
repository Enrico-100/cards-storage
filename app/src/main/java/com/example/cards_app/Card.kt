package com.example.cards_app

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val number: Int,
    val name: String,
    val nameOfCard: String,
    val picture: String?
)
