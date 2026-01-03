package com.example.cards_app.communication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cards_app.LocalCardsRepository

// This factory knows how to create our CommunicationViewModel.
class CommunicationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunicationViewModel::class.java)) {
            // 1. Create the repository.
            val localCardRepo = LocalCardsRepository(context.applicationContext)
            // 2. Pass the repository into the ViewModel's constructor.
            @Suppress("UNCHECKED_CAST")
            return CommunicationViewModel(localCardRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
