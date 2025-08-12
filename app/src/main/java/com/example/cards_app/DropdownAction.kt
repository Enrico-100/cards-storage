package com.example.cards_app

data class DropdownAction(
    val title: String,
    val onClick: () -> Unit
)
