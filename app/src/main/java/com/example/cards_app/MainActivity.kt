package com.example.cards_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.cards_app.ui.theme.Cards_appTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var numberOfScreen by remember { mutableIntStateOf(0) }

            val dropdownMenuItems: List<DropdownAction> = listOf(
                DropdownAction("Your cards", onClick = { numberOfScreen = 0 }, icon = R.drawable.outline_account_balance_wallet_24),
                DropdownAction("Add card", onClick = { numberOfScreen = 1 }, icon = R.drawable.outline_add_card_24),
                DropdownAction("Account", onClick = { numberOfScreen = 2 }, icon = R.drawable.outline_account_circle_24)
            )
            Cards_appTheme {
                val cards by viewModel.cards.collectAsState()
                val myCards = MyCards()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MyTopAppBar(title = "Cards App", dropdownMenuItems = dropdownMenuItems, onTitleClick = { numberOfScreen = 0 })
                    }
                ) { innerPadding ->
                    when (numberOfScreen) {
                        0 -> {
                            LazyColumn(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                item {
                                    Greeting(
                                        name = "Android",
                                    )
                                }
                                items(cards) {
                                    myCards.Cards(card = it, viewModel = viewModel)
                                }
                                item {
                                    myCards.NoCardsYet(
                                        onCardClick = { numberOfScreen = 1 }
                                    )
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
                                AddCard().MyAddCard(viewModel = viewModel, onButtonClick = {
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
fun MyTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    dropdownMenuItems: List<DropdownAction> = listOf(),
    onTitleClick: () -> Unit = {}
) {
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
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = actionItem.icon),
                                    contentDescription = actionItem.title
                                )
                            },
                            text = { Text(
                                actionItem.title,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            ) },
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