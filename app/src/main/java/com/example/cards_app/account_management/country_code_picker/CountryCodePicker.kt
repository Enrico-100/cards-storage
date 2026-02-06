package com.example.cards_app.account_management.country_code_picker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("assignedValueIsNeverRead")
fun CountryCodePicker(
    modifier: Modifier = Modifier,
    number: String = "",
    onNumberChange: (Pair<String, String>) -> Unit,
    label: String = "Phone Number",
    singleLine: Boolean = false,
    enabled: Boolean = true,
    initialCountry: String = "SI",
    onCountryChange: (Country) -> Unit = {}
) {
    var exposed by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(Countries.countries.find { it.initials == initialCountry }) }
    var searchText by remember { mutableStateOf("") }
    val filteredList = remember(searchText, Countries.countries) {
        if (searchText.isEmpty()) {
            Countries.countries
        } else {
            Countries.countries.filter {
                it.initials.contains(searchText, ignoreCase = true) ||
                        it.name.contains(searchText, ignoreCase = true)
            }
        }
    }
    ExposedDropdownMenuBox(
        expanded = exposed,
        onExpandedChange = {
            exposed = !exposed
        }
    ) {
        OutlinedTextField(
            value = number,
            onValueChange = {
                val transformation = selectedCountry?.visualTransformation as? NumberVisualTransformation
                if(it.length <= (transformation?.maxLength ?: 12)){
                    val fullNumber = selectedCountry?.code.toString() + it
                    onNumberChange(Pair(it, fullNumber))
                }
            },
            label = { Text(label) },
            modifier = modifier,
            singleLine = singleLine,
            enabled = enabled,
            leadingIcon = {
                TextButton(
                    onClick = {
                        exposed = !exposed
                    },
                    enabled = enabled
                ) {
                    Text(
                        text = (selectedCountry?.flag ?: "")+" +"+ selectedCountry?.code.toString(),
                        modifier = Modifier.fillMaxHeight()
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = exposed
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = selectedCountry?.visualTransformation ?: VisualTransformation.None,
            placeholder = {
                Text(
                    text = selectedCountry?.placeholder ?: ""
                )
            }
        )
        DropdownMenu(
            expanded = exposed,
            onDismissRequest = {
                exposed = false
                searchText = ""
            },
            modifier = Modifier.exposedDropdownSize(),
            properties = PopupProperties(focusable = true)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            filteredList.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = country.flag,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = country.name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = country.initials,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "+"+country.code.toString(),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    onClick = {
                        selectedCountry = country
                        exposed = false
                        searchText = ""
                        onCountryChange(country)
                    },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}