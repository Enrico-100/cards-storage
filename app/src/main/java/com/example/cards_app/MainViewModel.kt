package com.example.cards_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel (application: Application) : AndroidViewModel(application) {
    private val dataStore = AppDataStore(application)

    val cards: StateFlow<List<Card>> = dataStore.cardsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun addCardAndSave(newCard: Card) {
        viewModelScope.launch {
            val currentCards = cards.value.toMutableList()
            currentCards.add(newCard)
            dataStore.saveCards(currentCards)
        }
    }
    fun persistCurrentCards() { // Example if you need to save explicitly at some point
        viewModelScope.launch {
            dataStore.saveCards(cards.value)
        }
    }
}