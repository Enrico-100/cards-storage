package com.example.cards_app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cards_app.add_card.BarcodeGeneratorAndSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel (application: Application) : AndroidViewModel(application) {
    private val localCardsRepo = LocalCardsRepository(application)//a way to communicate with datastore

    //--navigation state--//
    // screenStack //
    private val _screenStack = MutableStateFlow(listOf(0))//screen stack
    val screenStack = _screenStack.asStateFlow()
    // cardStack //
    private val _cardStack = MutableStateFlow(listOf<Card?>(null))//card stack
    val cardStack = _cardStack.asStateFlow()
    // editStack //
    private val _editStack = MutableStateFlow(listOf(false))//edit stack
    val editStack = _editStack.asStateFlow()

    //--loading state--//
    private val _isLoading = MutableStateFlow(true)//loading state
    val isLoading = _isLoading.asStateFlow()

    //--navigation logic--//
    fun navigateTo(screen: Int, edit: Boolean = false, cardToEdit: Card? = null) { //function that navigates to any screen and sets the current card and edit state
        val numberOfScreen = screenStack.value.last()//current screen
        val currentCard = cardStack.value.last()//current card
        val currentEditState = editStack.value.last()//current edit state

        if (screen == numberOfScreen && currentCard == cardToEdit && currentEditState == edit) return

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
        _editStack.update{ stack ->
            val newStack = stack + edit
            if (newStack.size > 10) newStack.drop(1) else newStack
        }
    }
    fun navigateBack() {//function that navigates back to the previous screen
        if (_screenStack.value.size > 1){
            _screenStack.update{ stack -> stack.dropLast(1)}
            _cardStack.update{ stack -> stack.dropLast(1)}
            _editStack.update{ stack -> stack.dropLast(1)}
        }else {
            _screenStack.value = listOf(0)
            _cardStack.value = listOf(null)
            _editStack.value = listOf(false)
        }
    }
    fun deleteCardFromStack(cardToDeleted: Card?) {
        if (cardToDeleted == null) return
        deleteCardByID(cardToDeleted.id)
        val zippedStacks = _screenStack.value.zip(_cardStack.value).zip(_editStack.value) { (screen, card), edit -> Triple(screen, card, edit) }
        val newStacks = zippedStacks.filter { it.second?.id != cardToDeleted.id }
        if (newStacks.isEmpty()) {
            _screenStack.value = listOf(0)
            _cardStack.value = listOf(null)
            _editStack.value = listOf(false)
        }else {
            _screenStack.value = newStacks.map { it.first }
            _cardStack.value = newStacks.map { it.second }
            _editStack.value = newStacks.map { it.third }
        }
        navigateTo(0)
    }

    fun replaceScreenOnStack(screen: Int) {//takes the screen to use when replacing current screen
        val currentScreen = screenStack.value.last()
        if (currentScreen == screen) return
        val newStack = _screenStack.value.map { if (it == currentScreen) screen else it }
        _screenStack.value = newStack
    }
    val cards: StateFlow<List<Card>> = localCardsRepo.cardsFlow//cards read from disk
        .onEach {
            delay(20)
            _isLoading.value = false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    //--add card and save logic--//
    fun addCardAndSave(newCard: Card, onSuccess: () -> Unit) {//add card and save to disk
        viewModelScope.launch {
            localCardsRepo.saveCard(newCard)
            onSuccess()
        }
    }
    fun deleteCardByID(id: String) {//delete card by id
        viewModelScope.launch {
            localCardsRepo.deleteCardByID(id)
        }
    }

    fun overwriteLocalCards(cards: List<Card>) {//overwrite local cards
        viewModelScope.launch {
            localCardsRepo.overwriteAll(cards)
            Log.d("MainViewModel", "Local cards overwritten")
        }
    }

    //--card regeneration logic--//
    fun regenerateCardImage(card: Card, context: android.content.Context) {
        // 1. Launch on IO thread (Prevents UI Freeze)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val generator = BarcodeGeneratorAndSaver()
                // 2. Generate the Bitmap (Heavy CPU task)
                val bitmap = generator.generateBarCode(card.number, card.codeType)
                // 3. Save to File (Heavy I/O task)
                // Using the card ID as the filename keeps it unique and consistent
                val newPath = generator.saveBitmapToFile(context, bitmap, card.id)
                if (newPath != null) {
                    val currentList = cards.value
                    val latestCard = currentList.find { it.id == card.id }

                    if (latestCard != null) {
                        // 4. Create a copy of the LATEST card with the updated path
                        // This ensures we keep any color/name changes made while the image was generating.
                        val updatedCard = latestCard.copy(picture = newPath)

                        // 5. Update the database
                        localCardsRepo.saveCard(updatedCard)
                        Log.d("MainViewModel", "Regenerated and saved barcode for ${latestCard.name}")
                    } else {
                        Log.w("MainViewModel", "Card ${card.id} was deleted during regeneration.")
                    }
                } else {
                    Log.e("MainViewModel", "Failed to save generated bitmap for ${card.name}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error regenerating card: ${e.message}")
            }
        }
    }

    // settings logic //
    private val _settings = AppDataStore(application.applicationContext)
    val settingsFlow: StateFlow<String?> = _settings.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun saveSettings(newSettings: String) {
        viewModelScope.launch {
            _settings.saveSettings(newSettings)
        }
    }

}