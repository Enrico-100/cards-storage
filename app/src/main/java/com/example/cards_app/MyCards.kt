package com.example.cards_app

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.example.cards_app.add_card.Templates
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

class MyCards {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Cards2(
        cards: List<Card>,
        onEditClick: (Card) -> Unit = {},
        onDeleteClick: (Card) -> Unit = {},
        onRegenerate: (Card) -> Unit = {},
        noCardsYetClick: () -> Unit = {},
        onReorder: (List<Card>) -> Unit = {}
    ){
        val showCard = remember { mutableStateOf(false) }
        val showEditDialog = remember { mutableStateOf(false) }
        val showDeleteDialog = remember { mutableStateOf(false) }
        val currentCardIndex = remember { mutableStateOf<Int?>(null) }
        var reorderedCards by remember(cards){ mutableStateOf(cards) }
        val lazyGridState = rememberLazyGridState()
        val reorderableLazyGridState = rememberReorderableLazyGridState(
            lazyGridState = lazyGridState,
            onMove = { from, to ->
                reorderedCards = reorderedCards.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
                onReorder(reorderedCards)
            }
        )
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        if (showEditDialog.value && currentCardIndex.value != null) {
            onEditClick(reorderedCards[currentCardIndex.value!!])
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
                            onDeleteClick(reorderedCards[currentCardIndex.value!!])
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(7.dp),
            state = lazyGridState
        ){
            items(count = reorderedCards.size, key = { reorderedCards[it].id }) { cardIndex ->
                ReorderableItem(reorderableLazyGridState, key = reorderedCards[cardIndex].id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 10.dp else 0.dp)
                    val scale by animateFloatAsState(if (isDragging) 0.95f else 1f, label = "scale")
                    val alpha by animateFloatAsState(if (isDragging) 0.8f else 1f, label = "alpha")
                    val card = reorderedCards[cardIndex]
                    val template =
                        Templates.list.find { it.nameOfCard.equals(card.nameOfCard, ignoreCase = true) }
                    val logoResId = template?.logoResId
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(7.dp)
                            .clickable(onClick = {
                                showCard.value = true
                                currentCardIndex.value = cardIndex
                            })
                            .draggableHandle(
                                dragGestureDetector = DragGestureDetector.LongPress
                            )
                            .shadow(elevation)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                alpha = alpha
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(
                                template?.color?.toColorInt() ?: card.color.toColorInt()
                            )
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(100.dp)
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
                                                    Color.Black.copy(alpha = 0.1f),
                                                    Color.Black.copy(alpha = 0.5f)
                                                )
                                            )
                                        )
                                        .matchParentSize()
                                ) {
                                    Text(
                                        text = card.name,
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .align(Alignment.BottomStart),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                            } else {
                                val color = Color(card.color.toColorInt())
                                val blackOrWhite =
                                    if (color.luminance() > 0.5) Color.Black else Color.White
                                val initials = remember(card.nameOfCard) {
                                    val parts = card.nameOfCard.split(" ").filter { it.isNotBlank() }
                                    val lowerCaseParts = parts.map { it.lowercase() }

                                    val nIndex =
                                        lowerCaseParts.indexOfFirst { it == "n" || it == "and" }
                                    val ampersandIndex = lowerCaseParts.indexOfFirst { it == "&" }

                                    when {
                                        nIndex != -1 -> {
                                            val before = parts.subList(0, nIndex)
                                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                .joinToString("")
                                            val after = parts.subList(nIndex + 1, parts.size)
                                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                .joinToString("")
                                            "${before}n${after}"
                                        }

                                        ampersandIndex != -1 -> {
                                            val before = parts.subList(0, ampersandIndex)
                                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                .joinToString("")
                                            val after = parts.subList(ampersandIndex + 1, parts.size)
                                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                .joinToString("")
                                            "${before}&${after}"
                                        }

                                        parts.size >= 2 -> {
                                            parts.take(2)
                                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                .joinToString("")
                                        }

                                        parts.isNotEmpty() -> {
                                            parts.first().take(2).uppercase()
                                        }

                                        else -> "Card"
                                    }
                                }
                                Text(
                                    text = initials,
                                    modifier = Modifier.align(Alignment.Center),
                                    color = blackOrWhite,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = card.name,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.BottomStart),
                                    color = blackOrWhite,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
            item(
                span = {GridItemSpan(maxLineSpan)}
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
            val card = reorderedCards[currentCardIndex.value ?: return]
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