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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel (application: Application) : AndroidViewModel(application) {
    private val dataStore = AppDataStore(application)//datastore

    //--navigation state--//
    private val _screenStack = MutableStateFlow(listOf(0))//screen stack
    val screenStack = _screenStack.asStateFlow()

    private val _cardStack = MutableStateFlow(listOf<Card?>(null))//card stack
    val cardStack = _cardStack.asStateFlow()

    private val _editState = MutableStateFlow(false)//edit state
    val editState = _editState.asStateFlow()

    //--loadnig state--//
    private val _isLoading = MutableStateFlow(true)//loading state
    val isLoading = _isLoading.asStateFlow()

    //--navigation logic--//
    fun navigateTo(screen: Int, edit: Boolean = false, cardToEdit: Card? = null) { //function that navigates to any screen and sets the current card and edit state
        val numberOfScreen = screenStack.value.last()//current screen
        val currentCard = cardStack.value.last()//current card

        if (screen == numberOfScreen && currentCard == cardToEdit) return
        _editState.value = edit

        val nextCard = // Case A: You explicitly said "Switch to THIS card"
            cardToEdit
                ?: if (screen == 3 || (screen == 1 && edit)) {
                    currentCard // Case B: "Keep looking at the same card"
                } else {
                    null // Case C: "Forget the card" (Home, Account, Add New)
                }

        // Update stacks
        _cardStack.update{ stack ->
            val newStack = stack + nextCard
            if (newStack.size > 10) newStack.drop(1) else newStack
        }
        _screenStack.update{ stack ->
            val newStack = stack + screen
            if (newStack.size > 10) newStack.drop(1) else newStack
        }
    }
    fun navigateBack() {
        _editState.value = false
        if (_screenStack.value.size > 1){
            _screenStack.update{ stack -> stack.dropLast(1)}
            _cardStack.update{ stack -> stack.dropLast(1)}
        }else {
            _screenStack.value = listOf(0)
            _cardStack.value = listOf(null)
        }
    }
    fun deleteCardFromStack(cardToDelete: Card?) {
        if (cardToDelete == null) return
        deleteCardByID(cardToDelete.id)
        val zipedStacks = _screenStack.value.zip(_cardStack.value)
        val newStacks = zipedStacks.filter { it.second != cardToDelete }
        if (newStacks.isEmpty()) {
            _screenStack.value = listOf(0)
            _cardStack.value = listOf(null)
        }else {
            _screenStack.value = newStacks.map { it.first }
            _cardStack.value = newStacks.map { it.second }
        }
        navigateTo(0)
    }

    val cards: StateFlow<List<Card>> = dataStore.cardsFlow//cards read from disk
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    //--add card logic--//
    fun addCardAndSave(newCard: Card) {//add card and save to disk
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
    fun deleteCardByID(id: String) {//delete card by id
        viewModelScope.launch {
            dataStore.deleteCardByID(id)
        }
    }

}