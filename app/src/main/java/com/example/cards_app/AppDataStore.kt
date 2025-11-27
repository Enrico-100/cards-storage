package com.example.cards_app

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.cardsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_cards_preferences")

class AppDataStore(private val context: Context) {

    companion object {
        val CARDS_KEY = stringPreferencesKey("cards_list_json")
    }
    val cardsFlow: Flow<List<Card>> = context.cardsDataStore.data
        .map { preferences ->
            val jsonString = preferences[CARDS_KEY]
            if (jsonString != null && jsonString.isNotEmpty()) {
                try {
                    Json.decodeFromString<List<Card>>(jsonString)
                }catch (e: Exception){
                    Log.e("AppDataStore", "Error decoding cards from JSON: ${e.message}")
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
        .catch { exception ->
            Log.e("AppDataStore", "Error reading cards from DataStore: ${exception.message}")
            emit(emptyList())
        }
    suspend fun saveCard(newCard: Card) {
        try {
            context.cardsDataStore.edit { preferences ->
                val currentJson = preferences[CARDS_KEY]
                val currentCards = if (currentJson != null && currentJson.isNotEmpty()) {
                    try {
                        Json.decodeFromString<MutableList<Card>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("AppDataStore", "Error decoding or reading cards from DataStore: ${e.message}")
                        mutableListOf()
                    }
                } else {
                    mutableListOf()
                }
                val existingCardIndex = currentCards.indexOfFirst { it.id == newCard.id }
                if (existingCardIndex != -1) {
                    currentCards[existingCardIndex] = newCard
                } else {
                    currentCards.add(newCard)
                }
                val jsonString = Json.encodeToString(currentCards)
                preferences[CARDS_KEY] = jsonString
                Log.d("AppDataStore", "Cards saved successfully. JSON: $jsonString")
            }
        }catch (e: Exception){
            Log.e("AppDataStore", "Error encoding or saving cards to DataStore: ${e.message}")

        }
    }
    suspend fun deleteCardByID(id: String) {
        try {
            context.cardsDataStore.edit { preferences ->
                // 1. Read current list safely
                val currentJson = preferences[CARDS_KEY]
                val currentCards = if (currentJson != null && currentJson.isNotEmpty()) {
                    try {
                        Json.decodeFromString<MutableList<Card>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("AppDataStore", "Error decoding or reading cards from DataStore: ${e.message}")
                        mutableListOf()
                    }
                } else {
                    mutableListOf()
                }

                // 2. Remove the card
                val initialSize = currentCards.size
                currentCards.removeAll { it.id == id }

                // 3. Save only if changed
                if (currentCards.size < initialSize) {
                    val jsonString = Json.encodeToString(currentCards)
                    preferences[CARDS_KEY] = jsonString
                    Log.d("AppDataStore", "Card with id $id deleted successfully.")
                } else {
                    Log.d("AppDataStore", "Card with id $id not found for deletion.")
                }
            }
        } catch (e: Exception) {
            Log.e("AppDataStore", "Error deleting card: ${e.message}")
        }
    }


}