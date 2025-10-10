package com.example.cards_app

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import java.util.UUID

class AddCard {
    @Composable
    fun MyAddCard(
        viewModel: MainViewModel,
        onButtonClick: () -> Unit = {},
        card: Card? = null
    ) {
        val context = LocalContext.current
        val id by remember(card?.id) { mutableStateOf(card?.id ?: UUID.randomUUID().toString()) }
        val bitmap = remember(card?.id) { mutableStateOf<Bitmap?>(null) }
        val savePath = remember(card?.id) { mutableStateOf(card?.picture) }
        var number by remember(card?.id) { mutableStateOf(card?.number ?: "") }
        var name by remember(card?.id) { mutableStateOf(card?.name ?: "") }
        var nameOfCard by remember(card?.id) { mutableStateOf(card?.nameOfCard ?: "") }
        val colors = listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Magenta,
            Color.Cyan,
            Color.Gray,
        )
        var color by remember(card?.id) { mutableStateOf( if (card != null) Color(card.color.toColorInt()) else colors.first()) }
        var validationMessage by remember(card?.id) { mutableStateOf("") }
        var showValidationMessage by remember(card?.id) { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ){
            OutlinedTextField(
                value = number,
                onValueChange = {
                    number = it
                },
                label = { Text("Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = nameOfCard,
                onValueChange = {
                    nameOfCard = it
                },
                label = { Text("Name of Card") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            LazyRow(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                items(colors.size) {
                    val currentColor = colors[it]
                    val isSelected = (color == currentColor)
                    ColorCircle(
                        color = colors[it],
                        isSelected =  isSelected,
                        onClick = {
                            color = currentColor
                        },

                    )

                }
            }
            Button(
                onClick = {
                    val colorString = color.toHexString()
                    if (number.isEmpty()){
                        validationMessage += "Number cannot be empty.\n"
                    }
                    if (name.isEmpty()){
                        validationMessage += "Name cannot be empty.\n"
                    }
                    if (nameOfCard.isEmpty()){
                        validationMessage += "Name of card cannot be empty.\n"
                    }
                    showValidationMessage = validationMessage.isNotEmpty()
                    if (showValidationMessage) {
                        return@Button
                    }
                    if (validationMessage.isEmpty()){
                        savePath.value = null
                        bitmap.value = BarcodeGeneratorAndSaver().generateBarCode(number)
                        savePath.value = BarcodeGeneratorAndSaver().saveBitmapToFile(context = context, bitmap.value!!, id)
                    }
                    if (savePath.value == null){
                        validationMessage += "Failed to save barcode.\n"
                    }
                    if(bitmap.value == null) {
                        validationMessage += "Failed to generate barcode."
                    }
                    showValidationMessage = validationMessage.isNotEmpty()
                    if (showValidationMessage) {
                        return@Button
                    }
                    val card = Card(id=id,number=number, name=name, nameOfCard=nameOfCard, picture=savePath.value.toString(), color=colorString)
                    viewModel.addCardAndSave(card)
                    onButtonClick()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Add Card")
            }
            if (showValidationMessage) {
                AlertDialog(
                    onDismissRequest = {
                        showValidationMessage = false
                        validationMessage = ""
                        },
                    title = { Text("Validation Error") },
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


@Composable
fun ColorCircle(
    color : Color,
    isSelected: Boolean,
    onClick: () -> Unit = {},
){
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color = color),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {onClick()}
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = if (color.luminance() > 0.5) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
fun Color.toHexString(): String{
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}