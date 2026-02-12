package com.example.cards_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.cards_app.account_management.AccountScreen
import com.example.cards_app.account_management.LogInScreen
import com.example.cards_app.account_management.RecoveryScreen
import com.example.cards_app.account_management.SignUpScreen
import com.example.cards_app.add_card.AddCard
import com.example.cards_app.add_card.DropdownAction
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
            val stackCard = remember(cardStack) { cardStack.last() }
            val editState = remember(editStack) { editStack.last() }
            val currentCard = remember(cards, stackCard) {
                if (stackCard != null) {
                    cards.find { it.id == stackCard.id } ?: stackCard
                } else {
                    null
                }
            }
            val settings by viewModel.settingsFlow.collectAsState()


            val dropdownMenuItems: List<DropdownAction> = listOf(
                DropdownAction(
                    "Your cards",
                    onClick = { viewModel.navigateTo(0) },
                    icon = R.drawable.outline_account_balance_wallet_24
                ),
                DropdownAction(
                    "Add card",
                    onClick = { viewModel.navigateTo(1) },
                    icon = R.drawable.outline_add_card_24
                ),
                DropdownAction(
                    "Account",
                    onClick = { viewModel.navigateTo(2) },
                    icon = R.drawable.outline_account_circle_24
                ),
                DropdownAction(
                    "Settings",
                    onClick = { viewModel.navigateTo(7) },
                    icon = R.drawable.outline_build_24
                )
            )
            val darkTheme = when (settings!!.split(" ")[0]){
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            Cards_appTheme(
                dynamicColor = true,
                darkTheme = darkTheme
            ) {

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
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                myCards.Cards2(
                                    cards = cards,
                                    onEditClick = {
                                        viewModel.navigateTo(1, true, it)
                                    },
                                    noCardsYetClick = {
                                        viewModel.navigateTo(1)
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteCardFromStack(it)
                                    },
                                    onRegenerate = {
                                        viewModel.regenerateCardImage(it, this@MainActivity)
                                    },
                                    onReorder = {
                                        viewModel.overwriteLocalCards(it)
                                    }
                                )
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
                                    card = if (editState) currentCard else null,
                                    onCancelClick = {
                                        viewModel.navigateBack()
                                    }
                                )
                            }
                        }
                        2 -> {// login screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                LogInScreen().MyLogInScreen(
                                    onSignUpClick = {
                                        viewModel.navigateTo(4)
                                    },
                                    onLoginSuccess = {
                                        viewModel.replaceScreenOnStack(5)
                                    },
                                    onForgotPasswordClick = {
                                        viewModel.navigateTo(6)
                                    }
                                )
                            }
                        }
                        4 -> {//sign up screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                SignUpScreen().MySignUpScreen(
                                    onSignUpSuccess = {
                                        viewModel.replaceScreenOnStack(5)
                                    }
                                )
                            }
                        }
                        5 -> {//account screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                AccountScreen().MyAccountScreen(
                                    onLogOut = {
                                        viewModel.replaceScreenOnStack(2)
                                        viewModel.navigateTo(0)
                                    }
                                )
                            }
                        }
                        6 -> {//recovery screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                RecoveryScreen(
                                    onRecoverySuccess = {
                                        viewModel.replaceScreenOnStack(2)
                                    }
                                )
                            }
                        }
                        7 -> {//settings screen
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                SettingsScreen().MySettingsScreen(
                                    settings = settings ?: "",
                                    onSettingsChange = {
                                        viewModel.saveSettings(it)
                                    },
                                    deleteAllCards = {
                                        viewModel.overwriteLocalCards(emptyList())
                                    }
                                )
                            }
                        }
                        else -> {
                            viewModel.navigateBack()
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
