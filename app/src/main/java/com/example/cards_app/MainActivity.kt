package com.example.cards_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.cards_app.ui.theme.Cards_appTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        setContent {
            val screenStack by viewModel.screenStack.collectAsState()
            val cardStack by viewModel.cardStack.collectAsState()
            val editStack by viewModel.editStack.collectAsState()
            val cards by viewModel.cards.collectAsState()
            val numberOfScreen = remember(screenStack) { screenStack.last() }
            val currentCard = remember(cardStack) { cardStack.last() }
            val editState = remember(editStack) { editStack.last() }


            val dropdownMenuItems: List<DropdownAction> = listOf(
                DropdownAction("Your cards", onClick = { viewModel.navigateTo(0) }, icon = R.drawable.outline_account_balance_wallet_24),
                DropdownAction("Add card", onClick = { viewModel.navigateTo(1) }, icon = R.drawable.outline_add_card_24),
                DropdownAction("Account", onClick = { viewModel.navigateTo(2) }, icon = R.drawable.outline_account_circle_24)
            )

            Cards_appTheme {

                val myCards = MyCards()

                BackHandler(screenStack.size > 1 || screenStack.last() != 0) {
                    viewModel.navigateBack()
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MyTopAppBar(title = "Cards App", dropdownMenuItems = dropdownMenuItems, onTitleClick = { viewModel.navigateTo(0) })
                    }
                ) { innerPadding ->
                    when (numberOfScreen) {
                        0 -> {//main cards screen
                            LazyColumn(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                items(cards) { card ->
                                    myCards.Cards(
                                        card = card,
                                        onCardClick = {
                                            viewModel.navigateTo(3, false,card)
                                        },
                                        onEditClick = {
                                            viewModel.navigateTo(1, true, card)
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteCardFromStack(card)
                                        },
                                        onRegenerate = {
                                            viewModel.regenerateCardImage(card, this@MainActivity)
                                        }
                                    )
                                }
                                item {
                                    myCards.NoCardsYet(
                                        onCardClick = { viewModel.navigateTo(1) },
                                        text = if (cards.isEmpty()) "No cards yet, add a card to get started." else "Add a card."
                                    )
                                }
                            }
                        }
                        1 -> {//add card / modify card screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                AddCard().MyAddCard(
                                    viewModel = viewModel,
                                    onButtonClick = {
                                        viewModel.navigateTo(0)
                                    },
                                    card = if (editState) currentCard else null
                                )
                            }
                        }
                        2 -> {//account screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                Greeting(
                                    name = "account screen",//TODO: add account screen
                                )
                            }
                        }
                        3 -> {//show one card only screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                myCards.ShowCard(
                                    card = currentCard,
                                    onEditClick = {
                                        viewModel.navigateTo(1, true, currentCard)
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteCardFromStack(currentCard)
                                    },
                                    onRegenerate = { card ->
                                        viewModel.regenerateCardImage(card, this@MainActivity)
                                    }
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