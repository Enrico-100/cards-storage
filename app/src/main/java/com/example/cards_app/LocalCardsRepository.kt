package com.example.cards_app

import android.content.Context
import kotlinx.coroutines.flow.Flow

// This repository is the single source of truth for local card data.
class LocalCardsRepository(context: Context) {

    private val dataStore = AppDataStore(context.applicationContext)

    val cardsFlow: Flow<List<Card>> = dataStore.cardsFlow

    suspend fun saveCard(newCard: Card) {
        dataStore.saveCard(newCard)
    }

    suspend fun deleteCardByID(id: String) {
        dataStore.deleteCardByID(id)
    }

    suspend fun overwriteAll(cards: List<Card>) {
        dataStore.overwriteAll(cards)
    }
}
