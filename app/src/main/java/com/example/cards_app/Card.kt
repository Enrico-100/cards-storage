package com.example.cards_app

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val id: String,//unique identifier for each card
    val number: String,//card number
    val name: String,
    val nameOfCard: String,
    val codeType: Int = 1,//barcode type
    val picture: String?,//path to barcode image
    val color: String//color of the card
)
