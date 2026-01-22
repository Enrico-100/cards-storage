package com.example.cards_app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.cards_app.models.User

class SignUpScreen {

    @Composable
    fun MySignUpScreen(
        onSignUpSuccess: () -> Unit = {},
        communicationViewModel: CommunicationViewModel = viewModel(
            factory = CommunicationViewModelFactory(LocalContext.current.applicationContext)
        )
    ){
        val uiState by communicationViewModel.uiState.collectAsState()


        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var clientSideError by remember { mutableStateOf<String?>(null) }

        if (uiState.isSuccess) {
            LaunchedEffect(Unit) {
                onSignUpSuccess()
                communicationViewModel.clearState() // Reset state after navigation

            }
        }

        // Handle errors from the ViewModel (API or network errors)
        uiState.error?.let { serverError ->
            if (uiState.isSuccess){
                AlertDialog(
                    onDismissRequest = { communicationViewModel.clearError() },
                    title = { Text("Registration") },
                    text = { Text(serverError) },
                    confirmButton = {
                        TextButton(onClick = { communicationViewModel.clearError() }) { Text("OK") }
                    }
                )
            } else {
                AlertDialog(
                    onDismissRequest = { communicationViewModel.clearError() },
                    title = { Text("Registration") },
                    text = { Text(serverError) },
                    confirmButton = {
                        TextButton(onClick = { communicationViewModel.clearError() }) { Text("OK") }
                    }
                )
            }
        }

        // Handle client-side validation errors
        @Suppress("assignedValueIsNeverRead")
        clientSideError?.let { localError ->
            AlertDialog(
                onDismissRequest = { clientSideError = null },
                title = { Text("Invalid Input") },
                text = { Text(localError) },
                confirmButton = {
                    TextButton(onClick = { clientSideError = null }) {
                        Text("OK")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Create your account",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    enabled = !uiState.isLoading
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    @Suppress("assignedValueIsNeverRead")
                    Button(
                        onClick = {
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                clientSideError = "Please enter a valid email address"
                            } else if (username.isBlank() || password.isBlank()) {
                                clientSideError = "Username and password cannot be empty"
                            } else if (communicationViewModel.validatePassword(password)) {
                                val user = User(
                                    username = username,
                                    passwordHash = password,
                                    email = email
                                )
                                communicationViewModel.performRegistration(user)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create account")
                    }
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
}