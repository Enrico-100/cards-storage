package com.example.cards_app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class AccountScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAccountScreen(){
        var loogedIn by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var validationMessage by remember { mutableStateOf("") }
        var showValidationMessage by remember { mutableStateOf(false) }
        var showPassword by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (loogedIn) {
                Text(
                    "Welcome, $username!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    content = { Text("Log out") },
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp),
                    onClick = {
                        loogedIn = false
                        username = ""
                        password = ""
                    }
                )
            } else {
                Text(
                    "Please log in or sign up",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 5.dp),
                    singleLine = true,
                    visualTransformation = if(!showPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    trailingIcon = {
                        IconButton(
                            onClick = { showPassword = !showPassword }
                        ) {
                            Icon(
                                imageVector = if(!showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if(!showPassword) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                Button(
                    content = { Text("Log in") },
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp),
                    onClick = {
                        if (username.isEmpty()) {
                            validationMessage += "Username cannot be empty.\n"
                        }
                        if (password.isEmpty()) {
                            validationMessage += "Password cannot be empty.\n"
                        }
                        showValidationMessage = validationMessage.isNotEmpty()
                        if (showValidationMessage) {
                            return@Button
                        }
                        loogedIn = true
                    }
                )
                if (showValidationMessage) {
                    AlertDialog(
                        onDismissRequest = {
                            showValidationMessage = false
                            validationMessage = ""
                        },
                        title = { Text("Validation error") },
                        text = { Text(validationMessage) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showValidationMessage = false
                                    validationMessage = ""
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }

    }
}