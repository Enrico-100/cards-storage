package com.example.cards_app

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import androidx.core.graphics.toColorInt

class MyCards {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Cards(
        card: Card,
        viewModel: MainViewModel,
        onCardClick: () -> Unit = {},
        onEditClick: () -> Unit = {},
    ) {
        val showDeleteDialog = remember { mutableStateOf(false) }
        val showEditDialog = remember { mutableStateOf(false) }
        if (showEditDialog.value) {
            onEditClick()
        }
        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text("Delete Card") },
                text = { Text("Are you sure you want to delete this card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCardByID(card.id)
                            showDeleteDialog.value = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog.value = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp)
                .clickable(onClick = onCardClick)
                ,
                colors = CardDefaults.cardColors(
                    containerColor = Color(card.color.toColorInt())
                )
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                val color = Color(card.color.toColorInt())
                val blackOrWhite = if (color.luminance() > 0.5) Color.Black else Color.White
                Row {
                    Column {
                        Text(
                            text = card.name,
                            modifier = Modifier.padding(top = 8.dp),
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            color = blackOrWhite
                        )
                        Text(
                            text = card.nameOfCard,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            color = blackOrWhite
                        )
                        Text(
                            text = card.number,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            color = blackOrWhite
                        )
                    }
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showEditDialog.value = true },

                        ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Card",
                            tint = blackOrWhite
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog.value = true },

                        ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Card",
                            tint = blackOrWhite
                        )
                    }
                }

                // Display the generated barcode image if the path exists
                if (card.picture != null) {
                    Log.d("DisplayImage", "Attempting to load image from: ${card.picture}")
                    AsyncImage(
                        model = File(card.picture), // <<<< Use Coil's AsyncImage
                        // Pass the File object from the path
                        contentDescription = "Barcode for ${card.nameOfCard}",
                        modifier = Modifier
                            .fillMaxWidth() // Let the image take available width
                            .padding(vertical = 8.dp)
                            .padding(end = 16.dp),
                        contentScale = ContentScale.FillWidth // Scale the image to fit within bounds
                        // while maintaining aspect ratio.
                        // You might also use ContentScale.FillWidth
                    )
                } else {
                    // Optional: Show a placeholder or message if no barcode image
                    Text(
                        text = "No barcode image available.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    @Composable
    fun NoCardsYet(
        onCardClick: () -> Unit = {},
        text: String
    ){
        Card(
            modifier = Modifier
                .clickable(
                onClick = onCardClick
                )
                .fillMaxWidth()
                .padding(7.dp),
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = text,
                    modifier = Modifier.padding(8.dp)
                        .weight(1f),
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
                Icon(
                    painter = painterResource(id = R.drawable.outline_add_card_24),
                    contentDescription = "Add card",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
    @Composable
    fun ShowCard(
        card: Card?,
        viewModel: MainViewModel,
        onEditClick: () -> Unit
    ){
        if (card != null) {
            Cards(
                card = card,
                viewModel = viewModel,
                onEditClick = onEditClick
            )
        }
    }
}