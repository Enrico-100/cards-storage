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
import kotlinx.coroutines.flow.first
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
    suspend fun saveCards(cards: List<Card>) {
        try {
            val jsonString = Json.encodeToString(cards)
            context.cardsDataStore.edit { preferences ->
                preferences[CARDS_KEY] = jsonString
            }
            Log.d("AppDataStore", "Cards saved successfully. JSON: $jsonString")
        }catch (e: Exception){
            Log.e("AppDataStore", "Error encoding or saving cards to DataStore: ${e.message}")

        }

    }
    suspend fun deleteCardByNumber(number: Int) {
        val currentCards = cardsFlow.first().toMutableList()//gets currentList<Card>
        val initialSize = currentCards.size //gets initial size
        currentCards.removeAll { it.number == number } //removes card with number
        if (currentCards.size < initialSize) {
            saveCards(currentCards)
            Log.d("AppDataStore", "Card with number $number deleted successfully.")
        }else{
            Log.d("AppDataStore", "Card with number $number not found for deletion.")
        }
    }

}