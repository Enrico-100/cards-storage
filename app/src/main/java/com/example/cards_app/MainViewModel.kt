package com.example.cards_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel (application: Application) : AndroidViewModel(application) {
    private val dataStore = AppDataStore(application)
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val cards: StateFlow<List<Card>> = dataStore.cardsFlow
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun addCardAndSave(newCard: Card) {
        viewModelScope.launch {
            val currentCards = cards.value.toMutableList()
            val existingCard = currentCards.indexOfFirst { it.id == newCard.id }
            if (existingCard != -1) {
                currentCards[existingCard] = newCard
            }else{
                currentCards.add(newCard)
            }
            dataStore.saveCards(currentCards)
        }
    }
//    fun persistCurrentCards() {
//        viewModelScope.launch {
//            dataStore.saveCards(cards.value)
//        }
//    }
    fun deleteCardByID(id: String) {
        viewModelScope.launch {
            dataStore.deleteCardByID(id)
        }
    }

}