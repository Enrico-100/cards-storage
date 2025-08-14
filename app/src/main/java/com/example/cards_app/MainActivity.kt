package com.example.cards_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu // Example icon
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cards_app.ui.theme.Cards_appTheme
import coil.compose.AsyncImage
import java.io.File


val cards = mutableListOf(
    Card(1, "Card 1", "Name of Card 1", R.drawable.num_1.toString()),
    Card(2, "Card 2", "Name of Card 2", null),
    Card(3, "Card 3", "Name of Card 3", R.drawable.num_3.toString()),
)
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var numberOfScreen by remember { mutableIntStateOf(0) }

            val dropdownMenuItems: List<DropdownAction> = listOf(
                DropdownAction("Home") { numberOfScreen = 0 },
                DropdownAction("Action 1") { numberOfScreen = 1 },
                DropdownAction("Action 2") { numberOfScreen = 2 }
            )
            Cards_appTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MyTopAppBar(title = "Cards App", dropdownMenuItems = dropdownMenuItems, onTitleClick = { numberOfScreen = 0 })
                    }
                ) { innerPadding ->
                    when (numberOfScreen) {
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                Greeting(
                                    name = "Android",
                                )
                                LazyColumn {
                                    items(cards){
                                        Cards(it)
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                Greeting(
                                    name = "Screen 1",
                                )
                                AddCard().MyAddCard(cards, onButtonClick = {
                                    numberOfScreen = 0
                                })
                            }
                        }
                        2 -> {
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                Greeting(
                                    name = "Screen 2",
                                )
                            }
                        }

                    }



                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(title: String, modifier: Modifier = Modifier, dropdownMenuItems: List<DropdownAction> = listOf(), onTitleClick: () -> Unit = {}) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier.clickable(
            onClick = { onTitleClick() }
        ),
        navigationIcon = {
            Box {
                IconButton(
                    onClick = { showDropdownMenu = !showDropdownMenu }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Main navigation menu"
                    )
                }
                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                ) {
                    dropdownMenuItems.forEach { actionItem ->
                        DropdownMenuItem(
                            text = { Text(actionItem.title) },
                            onClick = {
                                actionItem.onClick()
                                showDropdownMenu = false
                            }
                        )
                    }
                }
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Cards(card: Card) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(7.dp)
    ) {
        Column {
            Text(
                text = card.name,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .padding(top = 8.dp)
            )
            Text(
                text = card.nameOfCard,
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(
                text = card.number.toString(),
                modifier = Modifier.padding(start = 16.dp)
            )
            // Display the generated barcode image if the path exists
            if (card.picture != null) {
                Log.d("DisplayImage", "Attempting to load image from: ${card.picture}")
                AsyncImage(
                    model = File(card.picture), // <<<< Use Coil's AsyncImage
                    // Pass the File object from the path
                    contentDescription = "Barcode for ${card.nameOfCard}",
                    modifier = Modifier
                        .fillMaxWidth() // Let the image take available width
                        .height(100.dp) // Set a specific height for the barcode
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.FillWidth // Scale the image to fit within bounds
                    // while maintaining aspect ratio.
                    // You might also use ContentScale.FillWidth
                )
            } else {
                // Optional: Show a placeholder or message if no barcode image
                Text(
                    text = "No barcode image available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Cards_appTheme {
        Scaffold(
            topBar = {
                MyTopAppBar(title = "Preview App")
            }
        ) { innerPadding ->
            Greeting("Android", modifier = Modifier.padding(innerPadding))
        }
    }
}