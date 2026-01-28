package com.example.cards_app

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage

class MyCards {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Cards2(
        cards: List<Card>,
        onEditClick: (Card) -> Unit = {},
        onDeleteClick: (Card) -> Unit = {},
        onRegenerate: (Card) -> Unit = {},
        noCardsYetClick: () -> Unit = {}
    ){
        val showCard = remember { mutableStateOf(false) }
        val showEditDialog = remember { mutableStateOf(false) }
        val showDeleteDialog = remember { mutableStateOf(false) }
        val currentCardIndex = remember { mutableStateOf<Int?>(null) }
        // This will allow the sheet to expand to full height
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        if (showEditDialog.value && currentCardIndex.value != null) {
            onEditClick(cards[currentCardIndex.value!!])
        }
        if (showDeleteDialog.value && currentCardIndex.value != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text("Delete Card") },
                text = { Text("Are you sure you want to delete this card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCard.value = false
                            showDeleteDialog.value = false
                            onDeleteClick(cards[currentCardIndex.value!!])
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
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.padding(7.dp)
        ){
            items(count = cards.size) { cardIndex ->
                val card = cards[cardIndex]
                val template = Templates.list.find { it.nameOfCard.equals(card.nameOfCard, ignoreCase = true) }
                val logoResId = template?.logoResId
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                        .clickable(onClick = {
                            showCard.value = true
                            currentCardIndex.value = cardIndex
                        }),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(template?.color?.toColorInt() ?: card.color.toColorInt())
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .heightIn(max = 100.dp)
                    ) {
                        if (logoResId != null) {

                            Image(
                                painter = painterResource(id = logoResId),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.7f),
                                                Color.Black.copy(alpha = 0.3f)
                                            )
                                        )
                                    )
                                    .matchParentSize()
                            ){
                                Column{
                                    val blackOrWhite = Color.White
                                    Text(
                                        text = card.name,
                                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp),
                                        color = blackOrWhite
                                    )
                                    Text(
                                        text = card.nameOfCard,
                                        modifier = Modifier.padding(8.dp),
                                        color = blackOrWhite
                                    )
                                }
                            }

                        } else {
                            Column {
                                val color = Color(card.color.toColorInt())
                                val blackOrWhite =
                                if (color.luminance() > 0.5) Color.Black else Color.White
                                Text(
                                    text = card.name,
                                    modifier = Modifier.padding(
                                        start = 8.dp,
                                        top = 8.dp,
                                        end = 8.dp
                                    ),
                                    color = blackOrWhite
                                )
                                Text(
                                    text = card.nameOfCard,
                                    modifier = Modifier.padding(8.dp),
                                    color = blackOrWhite
                                )
                            }
                        }
                    }
                }
            }
            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                Card(
                    modifier = Modifier
                        .clickable(
                            onClick = noCardsYetClick
                        )
                        .fillMaxWidth()
                        .padding(7.dp),
                ){
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = if (cards.isEmpty()) "No cards yet, add a card to get started." else "Add a card.",
                            modifier = Modifier
                                .padding(8.dp)
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
        }
        if (showCard.value) {
            val card = cards[currentCardIndex.value ?: return]
            val template = Templates.list.find { it.nameOfCard.equals(card.nameOfCard, ignoreCase = true) }
            ModalBottomSheet(
                onDismissRequest = { showCard.value = false },
                containerColor = Color(template?.color?.toColorInt() ?: card.color.toColorInt()),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 88.dp),
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    val color = Color(template?.color?.toColorInt() ?: card.color.toColorInt())
                    val blackOrWhite = if (color.luminance() > 0.5) Color.Black else Color.White
                    Row {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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
                            model = card.picture, // <<<< Use Coil's AsyncImage
                            // Pass the File object from the path
                            contentDescription = "Barcode for ${card.nameOfCard}",
                            modifier = Modifier
                                .fillMaxWidth() // Let the image take available width
                                .padding(vertical = 8.dp)
                                .padding(end = 16.dp),
                            contentScale = ContentScale.FillWidth, // Scale the image to fit within bounds
                            onError = {
                                Log.e(
                                    "ImageLoad",
                                    "AsyncImage failed to load image. Regenerating..."
                                )
                                onRegenerate(card)
                            }
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
    }
}