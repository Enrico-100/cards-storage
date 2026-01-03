package com.example.cards_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cards_app.communication.CommunicationViewModel
import com.example.cards_app.communication.CommunicationViewModelFactory
import com.example.cards_app.models.User

class AccountScreen {
    @Composable
    fun MyAccountScreen(
        onLogOut: () -> Unit = {},
        communicationViewModel: CommunicationViewModel = viewModel(
            factory = CommunicationViewModelFactory(LocalContext.current.applicationContext)
        ),
        mainViewModel: MainViewModel = viewModel()
    ){
        val user by communicationViewModel.user.collectAsState()
        val uiState by communicationViewModel.uiState.collectAsState()

        if (user == null) {
            LaunchedEffect(Unit) {
                onLogOut()
            }
            return
        }

        uiState.error?.let {
            AlertDialog(
                onDismissRequest = { communicationViewModel.clearError() },
                title = { Text("Error") },
                text = { Text(it) },
                confirmButton = {
                    TextButton(onClick = { communicationViewModel.clearError() }) { Text("OK") }
                }
            )
        }
        
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Welcome back, ${user!!.name ?: user!!.username}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // --- Show the sync card ---
                    item{
                        SyncCardsCard(
                            currentUser = user!!,
                            isLoading = uiState.isLoading,
                            onSyncClick = { cards -> communicationViewModel.syncCards(cards) },
                            mainViewModel = mainViewModel
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }


                    // --- Conditionally show Email and Phone verification ---
                    if (user?.emailVerified == false) {
                        item {
                            VerificationCard(
                                type = "Email",
                                target = user!!.email,
                                onVerifyClick = { code -> communicationViewModel.verifyEmail(code) },
                                isVerifying = uiState.isLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    if (user?.phoneVerified == false) {
                        item {
                            VerificationCard(
                                type = "Phone",
                                target = user!!.phoneNumber,
                                onVerifyClick = { code -> communicationViewModel.verifyPhone(code) },
                                isVerifying = uiState.isLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // --- Show the update user card ---
                    item {
                        UpdateUserCard(
                            currentUser = user!!,
                            isLoading = uiState.isLoading,
                            onUpdateClick = { updatedUser ->
                                communicationViewModel.updateUser(updatedUser)
                            },
                            communicationViewModel = communicationViewModel
                        )
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Action Buttons ---
                Button(onClick = { communicationViewModel.performLogout() }) {
                    Text("Log out")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { communicationViewModel.deleteUser() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            }
            if (uiState.isLoading) {
                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    /**
     * A reusable Composable for both email and phone verification.
     */
    @Composable
    private fun VerificationCard(
        type: String,
        target: String?,
        onVerifyClick: (String) -> Unit,
        isVerifying: Boolean
    ) {
        var verificationCode by remember { mutableStateOf("") }

        if (target.isNullOrBlank()) return // Don't show card if there's no email/phone to verify

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Verify Your $type",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "A verification code was sent to $target. Please enter it below.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Verification Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isVerifying
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVerifyClick(verificationCode) },
                    enabled = !isVerifying,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Verify")
                }
            }
        }
    }
    /**
     * A new card for updating user details with its own state and validation.
     */
    @Composable
    private fun UpdateUserCard(
        currentUser: User,
        isLoading: Boolean,
        onUpdateClick: (User) -> Unit,
        communicationViewModel: CommunicationViewModel
    ) {
        // State for the text fields, pre-filled with current user data
        var name by remember { mutableStateOf(currentUser.name ?: "") }
        var email by remember { mutableStateOf(currentUser.email ?: "") }
        var phoneNumber by remember { mutableStateOf(currentUser.phoneNumber ?: "") }
        var newPassword by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var clientSideError by remember { mutableStateOf<String?>(null) }


        // Show client-side validation errors in a dialog
        clientSideError?.let {
            AlertDialog(
                onDismissRequest = { clientSideError = null },
                title = { Text("Invalid Input") },
                text = { Text(it) },
                confirmButton = {
                    TextButton(onClick = { clientSideError = null }) { Text("OK") }
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Update Your Details", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading)
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // --- Client-side validation ---
                        val isPhoneValid = phoneNumber.isBlank() || android.util.Patterns.PHONE.matcher(phoneNumber).matches()
                        val isEmailValid = email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

                        if (!isEmailValid) {
                            clientSideError = "Please enter a valid email address."
                        } else if (!isPhoneValid) {
                            clientSideError = "Please enter a valid phone number."
                        } else if(newPassword.isBlank()|| communicationViewModel.validatePassword(newPassword)){
                            // Validation passed, create the updated User object
                            val updatedUser = User(
                                id = currentUser.id, // ID is needed for the backend to find the user
                                username = currentUser.username, // Username is immutable
                                name = name.takeIf { it.isNotBlank() },
                                email = email.takeIf { it.isNotBlank() },
                                phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
                                passwordHash = newPassword.takeIf { it.isNotBlank() } // Send new password only if it's not blank
                                
                            )
                            onUpdateClick(updatedUser)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Update")
                }
            }
        }
    }

    /**
     * A card that shows the sync status between local and server cards
     * and allows the user to choose a sync direction.
     */
    @Composable
    private fun SyncCardsCard(
        currentUser: User,
        isLoading: Boolean,
        onSyncClick: (List<Card>) -> Unit, // The action to sync cards to the server
        mainViewModel: MainViewModel
    ) {
        val localCards by mainViewModel.cards.collectAsState()
        val serverCards = currentUser.cards ?: emptyList()

        // --- Compare the lists to find differences ---
        val localOnlyCards = localCards.filter { local -> serverCards.none { it.id == local.id } }
        val serverOnlyCards = serverCards.filter { server -> localCards.none { it.id == server.id } }
        val isOutOfSync = localOnlyCards.isNotEmpty() || serverOnlyCards.isNotEmpty()

        // Only show the card if there's a need to sync.
        if (isOutOfSync) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sync Your Cards", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (localOnlyCards.isNotEmpty()) {
                        Text("${localOnlyCards.size} card(s) exist only on this device.")
                    }
                    if (serverOnlyCards.isNotEmpty()) {
                        Text("${serverOnlyCards.size} card(s) exist only on the server.")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- Sync Buttons ---
                        Button(
                            onClick = {
                                // User wants to PUSH local cards to the server.
                                // We merge the lists and send the complete list.
                                val mergedList = (serverCards + localOnlyCards).distinctBy { it.id }
                                onSyncClick(mergedList)
                            },
                            enabled = !isLoading
                        ) {
                            Text("Upload to Server")
                        }

                        Button(
                            onClick = {
                                // Call the MainViewModel function to overwrite local data
                                mainViewModel.overwriteLocalCards(serverCards)
                            },
                            enabled = !isLoading
                        ) {
                            Text("Download from Server")
                        }
                    }
                }
            }
        }
    }
}