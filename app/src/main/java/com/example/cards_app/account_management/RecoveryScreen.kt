package com.example.cards_app.account_management

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cards_app.communication.CommunicationViewModel
import com.example.cards_app.communication.CommunicationViewModelFactory

// Define an enum to manage the state of the recovery flow
enum class RecoveryStep {
    ENTER_USERNAME,
    CHOOSE_CHANNEL,
    ENTER_CODE_AND_RESET,
    SUCCESS
}

@Composable
fun RecoveryScreen(
    onRecoverySuccess: () -> Unit,
    communicationViewModel: CommunicationViewModel = viewModel(
        factory = CommunicationViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by communicationViewModel.uiState.collectAsState()
    val step by communicationViewModel.recoveryStep.collectAsState()

    // State for the input fields
    var username by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("") }
    var selectedChannelValue by remember { mutableStateOf("") }
    var recoveryCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // This will hold the options returned from the server
    val recoveryOptions = uiState.recoveryOptions

    // Navigate on successful reset
    if (uiState.isSuccess && step == RecoveryStep.SUCCESS) {
        LaunchedEffect(Unit) {
            onRecoverySuccess()
            communicationViewModel.resetRecoveryStep()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            communicationViewModel.resetRecoveryStep()
        }
    }

    // Show error dialogs
    uiState.error?.let {
        AlertDialog(
            onDismissRequest = { communicationViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { communicationViewModel.clearError() }) { Text("OK") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Password Recovery",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Conditionally show UI based on the current step
            when (step) {
                RecoveryStep.ENTER_USERNAME -> {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Enter your username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                                communicationViewModel.initiateRecovery(username)
                            },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Find Account")
                    }
                    // When recovery options are successfully fetched, move to the next step
                    if (!recoveryOptions.isNullOrEmpty()) {
                        Log.d("RecoveryScreen", "Recovery options: $recoveryOptions")
                    }
                }

                RecoveryStep.CHOOSE_CHANNEL -> {
                    Text("Select where to send the recovery code for user: $username")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recoveryOptions ?: emptyList()) { option ->
                            Button(
                                onClick = {
                                    selectedChannel = option.channel.toString()
                                    selectedChannelValue = option.maskedValue
                                    communicationViewModel.sendRecoveryCode(username, selectedChannel)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Send code to ${option.maskedValue}")
                            }
                        }
                    }
                }

                RecoveryStep.ENTER_CODE_AND_RESET -> {
                    Text("A code has been sent to $selectedChannelValue for user: $username")
                    if (selectedChannel == "EMAIL") {
                        Text("Please check your spam folder.")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = recoveryCode,
                        onValueChange = { recoveryCode = it },
                        label = { Text("Recovery Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading,
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
                            if (communicationViewModel.validatePassword(newPassword)) {
                                communicationViewModel.resetPassword(
                                    username,
                                    recoveryCode,
                                    newPassword
                                )
                            }
                            },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Password")
                    }
                }

                RecoveryStep.SUCCESS -> {
                    Text("Password reset successful!")
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
