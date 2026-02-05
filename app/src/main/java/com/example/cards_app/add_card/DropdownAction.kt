package com.example.cards_app.add_card

data class DropdownAction(
    val title: String,
    val onClick: () -> Unit,
    val icon: Int,
)