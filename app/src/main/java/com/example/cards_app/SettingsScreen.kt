package com.example.cards_app

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class SettingsScreen {
    @Suppress("AssignedValueIsNeverRead")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MySettingsScreen(
        settings: String,
        onSettingsChange: (String) -> Unit,
        deleteAllCards: () -> Unit,
        onExportCards: () -> Unit,
        onMergeCards: () -> Unit,
        onOverwriteCards: () -> Unit
    ) {
        var themeSelector by remember { mutableStateOf(false) }
        val settingsList = remember(settings) { settings.split(" ") }
        val theme = remember(settings) { settingsList[0] }
        val interactionSource = remember { MutableInteractionSource() }
        if (theme.isEmpty()){
            val newSettings = settingsList.toMutableList()
            newSettings[0] = "System"
            onSettingsChange(newSettings.joinToString(" "))
        }
        var deleteAllCardsDialog by remember { mutableStateOf(false) }
        var showCreditsDialog by remember { mutableStateOf(false) }
        var showImportCardsDialog by remember { mutableStateOf(false) }

        if (themeSelector) {
            AlertDialog(
                onDismissRequest = {
                    themeSelector = false
                },
                title = { Text("Select theme") },
                confirmButton = {
                    Button(
                        onClick = {
                            themeSelector = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable {
                                    val newSettings = settingsList.toMutableList()
                                    newSettings[0] = "Light"
                                    onSettingsChange(newSettings.joinToString(" "))
                                    Log.d("SettingsScreen", "New settings: $newSettings")
                                }
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == "Light"),
                                onClick = {
                                    val newSettings = settingsList.toMutableList()
                                    newSettings[0] = "Light"
                                    onSettingsChange(newSettings.joinToString(" "))
                                    Log.d("SettingsScreen", "New settings: $newSettings")
                                },
                                interactionSource = interactionSource,
                            )
                            Text(
                                text = "Light",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable {
                                    val newSettings = settingsList.toMutableList()
                                    newSettings[0] = "Dark"
                                    onSettingsChange(newSettings.joinToString(" "))
                                    Log.d("SettingsScreen", "New settings: $newSettings")
                                }
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == "Dark"),
                                onClick = {
                                    val newSettings = settingsList.toMutableList()
                                    newSettings[0] = "Dark"
                                    onSettingsChange(newSettings.joinToString(" "))
                                    Log.d("SettingsScreen", "New settings: $newSettings")
                                },
                                interactionSource = interactionSource,
                            )
                            Text(
                                text = "Dark",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable {
                                    val newSettings = settingsList.toMutableList()
                                    newSettings[0] = "System"
                                    onSettingsChange(newSettings.joinToString(" "))
                                    Log.d("SettingsScreen", "New settings: $newSettings")
                                }
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == "System"),
                                onClick = {
                                    val newSettings = settingsList.toMutableList()
                                    newSettings[0] = "System"
                                    onSettingsChange(newSettings.joinToString(" "))
                                    Log.d("SettingsScreen", "New settings: $newSettings")
                                },
                                interactionSource = interactionSource,
                            )
                            Text(
                                text = "System",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            )
        }
        if (deleteAllCardsDialog){
            AlertDialog(
                onDismissRequest = {
                    deleteAllCardsDialog = false
                },
                title = { Text("Delete all cards?") },
                text = {
                    Column{
                        Text(
                            text = "Are you sure you want to delete all cards?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "This action cannot be undone!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            deleteAllCardsDialog = false
                            deleteAllCards()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            deleteAllCardsDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        if (showCreditsDialog) {
            CreditsDialog(onDismiss = { showCreditsDialog = false })
        }
        if (showImportCardsDialog) {
            AlertDialog(
                onDismissRequest = { showImportCardsDialog = false },
                title = { Text("Import cards") },
                text = {
                    Column {
                        Text(
                            "How would you like to import the cards from the backup file?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "This action cannot be undone!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    showImportCardsDialog = false
                                    onMergeCards()
                                }
                            ) {
                                Text("Merge")
                            }
                            Button(
                                onClick = {
                                    showImportCardsDialog = false
                                    onOverwriteCards()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Overwrite")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    Button(
                        onClick = { showImportCardsDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsHeader("Settings")
            }
            item {
                Button(
                    onClick = {
                        themeSelector = !themeSelector
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Change theme")
                }
            }
            item {
                SettingsHeader("Data Management")
            }
            item {
                Button(
                    onClick = {
                        onExportCards()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Export cards")
                }
            }
            item {
                Button(
                    onClick = {
                        showImportCardsDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Import cards")
                }
            }
            item {
                Button(
                    onClick = {
                        deleteAllCardsDialog = !deleteAllCardsDialog
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete all cards")
                }
            }
            item {
                SettingsHeader("Info")
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "App Info",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(24.dp))
                    Column {
                        Text(
                            text = "Cards Storage",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Version: ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                SettingsHeader("Support me!")
            }
            item {
                val uriHandler = LocalUriHandler.current
                SettingsItem(
                    title = "Star on GitHub",
                    description = "Show your support by starring the project on GitHub!",
                    icon = Icons.Default.Favorite,
                    onClick = {
                        uriHandler.openUri("https://github.com/Enrico-100/cards-storage")
                    }
                )
            }
            item {
                SettingsHeader("Credits")
            }
            item {
                SettingsItem(
                    title = "Libraries & Resources",
                    description = "This app is built with these great open-source projects.",
                    icon = Icons.Default.Code,
                    onClick = { showCreditsDialog = true }
                )
            }
        }
    }

    @Composable
    fun SettingsHeader(
        title: String
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(start = 24.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
    @Composable
    private fun SettingsItem(
        title: String,
        description: String? = null,
        icon: ImageVector,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    @Composable
    fun CreditsDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Credits") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        CreditItem(
                            name = "Coil",
                            description = "An image loading library for Android, built on Kotlin Coroutines.",
                            url = "https://coil-kt.github.io/coil/"
                        )
                    }
                    item {
                        CreditItem(
                            name = "ColorPicker-Compose by skydoves",
                            description = "A beautiful and feature-rich color picker for Jetpack Compose.",
                            url = "https://github.com/skydoves/colorpicker-compose"
                        )
                    }
                    item {
                        CreditItem(
                            name = "Reorderable by Calvin Shroff",
                            description = "A modifier for reorderable drag-and-drop lists in Compose.",
                            url = "https://github.com/Calvin-LL/Reorderable"
                        )
                    }
                    item {
                        CreditItem(
                            name = "ZXing (Zebra Crossing)",
                            description = "An open-source, multi-format 1D/2D barcode image processing library.",
                            url = "https://github.com/zxing/zxing"
                        )
                    }
                    item {
                        CreditItem(
                            name = "Retrofit & OkHttp by Square",
                            description = "A type-safe HTTP client and an efficient HTTP & HTTP/2 client for Android and Java.",
                            url = "https://square.github.io/retrofit/"
                        )
                    }
                    item {
                        CreditItem(
                            name = "Kotlinx.serialization by JetBrains",
                            description = "The official Kotlin multiplatform and multi-format serialization library.",
                            url = "https://github.com/Kotlin/kotlinx.serialization"
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) { Text("Close") }
            }
        )
    }

    @Composable
    private fun CreditItem(name: String, description: String, url: String) {
        val uriHandler = LocalUriHandler.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri(url) }
                .padding(vertical = 4.dp)
        ) {
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}