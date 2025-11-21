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
            var screenStack by remember { mutableStateOf(listOf(0)) }//screen stack
            var cardStack by remember { mutableStateOf(listOf<Card?>(null)) }//card stack

            val numberOfScreen = remember(screenStack){screenStack.last()}//current screen
            val currentCard = remember(cardStack){cardStack.last()}//current card

            var editState by remember { mutableStateOf(false) }//edit state

            fun navigateTo(screen: Int, edit: Boolean = false, cardToEdit: Card? = null) { //function that navigates to any screen and sets the current card and edit state
                if (screen == numberOfScreen && currentCard == cardToEdit) return
                editState = edit

                val nextCard = // Case A: You explicitly said "Switch to THIS card"
                    cardToEdit
                        ?: if (screen == 3 || (screen == 1 && edit)) {
                            currentCard // Case B: "Keep looking at the same card"
                        } else {
                            null // Case C: "Forget the card" (Home, Account, Add New)
                        }

                val newCardStack = cardStack + nextCard
                val newStack = screenStack + screen

                if (newStack.size > 10) {
                    cardStack = newCardStack.drop(1)
                    screenStack = newStack.drop(1)
                } else {
                    cardStack = newCardStack
                    screenStack = newStack
                }

            }
            fun navigateBack() {
                editState = false
                if (screenStack.size > 1){
                    screenStack = screenStack.dropLast(1)
                    cardStack = cardStack.dropLast(1)
                }else {
                    screenStack = listOf(0)
                    cardStack = listOf(null)
                }
            }
            fun deleteCardFromStack(cardToDelete: Card?) {
                if (cardToDelete == null) return
                val zipedStacks = screenStack.zip(cardStack)
                val newStacks = zipedStacks.filter { it.second != cardToDelete }
                if (newStacks.isEmpty()) {
                    screenStack = listOf(0)
                    cardStack = listOf(null)
                }else {
                    screenStack = newStacks.map { it.first }
                    cardStack = newStacks.map { it.second }
                }
                navigateTo(0)
            }
            val dropdownMenuItems: List<DropdownAction> = listOf(
                DropdownAction("Your cards", onClick = { navigateTo(0) }, icon = R.drawable.outline_account_balance_wallet_24),
                DropdownAction("Add card", onClick = { navigateTo(1) }, icon = R.drawable.outline_add_card_24),
                DropdownAction("Account", onClick = { navigateTo(2) }, icon = R.drawable.outline_account_circle_24)
            )

            Cards_appTheme {
                val cards by viewModel.cards.collectAsState()
                val myCards = MyCards()

                BackHandler(screenStack.size > 1 || screenStack.last() != 0) {
                    navigateBack()
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MyTopAppBar(title = "Cards App", dropdownMenuItems = dropdownMenuItems, onTitleClick = { navigateTo(0) })
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
                                        viewModel = viewModel,
                                        onCardClick = {
                                            navigateTo(3, false,card)
                                        },
                                        onEditClick = {
                                            navigateTo(1, true, card)
                                        },
                                        onDeleteClick = {
                                            deleteCardFromStack(card)
                                        }
                                    )
                                }
                                item {
                                    myCards.NoCardsYet(
                                        onCardClick = { navigateTo(1) },
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
                                        navigateTo(0)
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
                                    viewModel = viewModel,
                                    onEditClick = {
                                        navigateTo(1, true, currentCard)
                                    },
                                    onDeleteClick = {
                                        deleteCardFromStack(currentCard)
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