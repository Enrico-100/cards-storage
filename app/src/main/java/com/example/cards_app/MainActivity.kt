package com.example.cards_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cards_app.ui.theme.Cards_appTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cards = mutableListOf<Card>(
            Card(1, "Card 1", "Name of Card 1"),
            Card(2, "Card 2", "Name of Card 2"),
            Card(3, "Card 3", "Name of Card 3"),
        )

        setContent {
            Cards_appTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MyTopAppBar(title = "Cards App")
                    }
                ) { innerPadding ->
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(title: String, modifier: Modifier = Modifier) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    //val dropdownMenuItems: List<DropdownAction> = emptyList(): List<DropdownAction>
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
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
                    val dropdownMenuItems = listOf("1", "2", "3")
                    dropdownMenuItems.forEach { actionItem ->
                        DropdownMenuItem(
                            //text = { Text(actionItem.title) },
                            text = { Text(actionItem) },
                            onClick = {
                                //actionItem.onClick()
                                showDropdownMenu = false
                            }
                        )
                    }
                }
            }

        },
        actions = {
            // Example action item:
            // IconButton(onClick = { /* TODO: Handle search icon click */ }) {
            //     Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
            // }
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
        modifier = Modifier.padding(7.dp).fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = card.name)
            Text(text = card.nameOfCard)
            Text(text = card.number.toString())
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