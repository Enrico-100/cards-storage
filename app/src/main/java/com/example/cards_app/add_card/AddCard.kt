package com.example.cards_app.add_card

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.cards_app.Card
import com.example.cards_app.MainViewModel
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.UUID

class AddCard {
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("assignedValueIsNeverRead")
    @Composable
    fun MyAddCard(
        viewModel: MainViewModel,
        onButtonClick: () -> Unit = {},
        card: Card? = null,
        onCancelClick: () -> Unit = {},
    ) {
        val context = LocalContext.current
        val id by remember(card?.id) { mutableStateOf(card?.id ?: UUID.randomUUID().toString()) }
        val bitmap = remember(card?.id) { mutableStateOf<Bitmap?>(null) }
        val codeType =
            remember(card?.id) { mutableIntStateOf(card?.codeType ?: Barcode.FORMAT_CODE_128) }
        val savePath = remember(card?.id) { mutableStateOf(card?.picture) }
        var number by remember(card?.id) { mutableStateOf(card?.number ?: "") }
        var name by remember(card?.id) { mutableStateOf(card?.name ?: "") }
        var nameOfCard by remember(card?.id) { mutableStateOf(card?.nameOfCard ?: "") }
        val colors = listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Magenta,
            Color.Gray,
        )
        var color by remember(card?.id) { mutableStateOf(if (card != null) Color(card.color.toColorInt()) else colors.first()) }
        var validationMessage by remember(card?.id) { mutableStateOf("") }
        var showValidationMessage by remember(card?.id) { mutableStateOf(false) }
        var showColorPicker by remember { mutableStateOf(false) }

        // --- Scanner state and permission
        var showScanner by remember { mutableStateOf(false) }
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val permissionLuncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCameraPermission = granted
                if (granted) {
                    showScanner = true
                }
            }
        )

//        LaunchedEffect(true) {
//            if (!hasCameraPermission) {
//                permissionLuncher.launch(Manifest.permission.CAMERA)
//            } else {
//                showScanner = true
//            }
//        }

        if (showScanner && hasCameraPermission) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CameraView(
                    onCodeScanned = { value, format ->
                        number = value
                        showScanner = false
                        codeType.intValue = format
                    }
                )
                ScannerOverlay(Modifier.fillMaxSize())
                Text(
                    text = "Scan barcode or QR code",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Button(
                    onClick = { showScanner = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = "Cancel")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                    },
                    label = { Text("Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (hasCameraPermission) {
                                    showScanner = true
                                } else {
                                    permissionLuncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan code",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
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
                var exposed by remember { mutableStateOf(false) }
                val filteredList = remember(nameOfCard, Templates.list) {
                    if (nameOfCard.isEmpty()) {
                        Templates.list
                    } else {
                        Templates.list.filter {
                            it.nameOfCard.contains(nameOfCard, ignoreCase = true)
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = exposed && filteredList.isNotEmpty(),
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = nameOfCard,
                        onValueChange = {
                            nameOfCard = it
                            exposed = true
                        },
                        label = { Text("Type of Card") },
                        modifier = Modifier.fillMaxWidth()
                            .menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryEditable,
                                true
                            )
                            .onFocusChanged { focusState ->
                                exposed = focusState.isFocused
                            },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    exposed = !exposed
                                },
                            ) {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = exposed
                                )
                            }
                        }
                    )
                    if (filteredList.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = exposed,
                            onDismissRequest = {
                                exposed = false
                            }
                        ) {
                            filteredList.forEach { template ->
                                DropdownMenuItem(
                                    text = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(template.color.toColorInt()))

                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (Color(template.color.toColorInt()).luminance() > 0.5) {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(
                                                                    Color.Black.copy(alpha = 0.7f),
                                                                    Color.Transparent
                                                                )
                                                            )
                                                        } else {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    Color.Transparent
                                                                )
                                                            )
                                                        }
                                                    )
                                                    .padding(8.dp)
                                                    .heightIn(max = 50.dp)
                                            ) {
                                                Text(
                                                    text = template.nameOfCard.replaceFirstChar { it.uppercase() },
                                                    color = Color.White,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Image(
                                                    painter = painterResource(id = template.logoResId),
                                                    contentDescription = "${template.nameOfCard} Logo",
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        nameOfCard =
                                            template.nameOfCard.replaceFirstChar { it.uppercase() }
                                        exposed = false
                                    },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                val matchedTemplate by remember(nameOfCard) {
                    mutableStateOf(Templates.list.find {
                        it.nameOfCard.equals(
                            nameOfCard,
                            ignoreCase = true
                        )
                    })
                }
                LaunchedEffect(matchedTemplate) {
                    color = if (matchedTemplate != null) {
                        Color(matchedTemplate!!.color.toColorInt())
                    } else {
                        colors.first()
                    }
                }
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    items(colors.size) {
                        val currentColor = colors[it]
                        val isSelected = (color == currentColor)
                        ColorCircle(
                            color = colors[it],
                            isSelected = isSelected,
                            onClick = {
                                if (matchedTemplate == null) {
                                    color = currentColor
                                }
                            },

                            )
                    }
                    item {
                        val isCustomColorSelected = !colors.contains(color)
                        ColorCircle(
                            color = if (isCustomColorSelected) color else Color.DarkGray,
                            isCustomPicker = true,
                            isSelected = isCustomColorSelected,
                            onClick = {
                                if (matchedTemplate == null) {
                                    showColorPicker = true
                                }
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Button(
                        onClick = {
                            if (number.isEmpty()) {
                                validationMessage += "Number cannot be empty.\n"
                            }
                            if (name.isEmpty()) {
                                validationMessage += "Name cannot be empty.\n"
                            }
                            if (nameOfCard.isEmpty()) {
                                validationMessage += "Type of card cannot be empty.\n"
                            }
                            showValidationMessage = validationMessage.isNotEmpty()
                            if (showValidationMessage) {
                                return@Button
                            }
                            if (validationMessage.isEmpty()) {
                                savePath.value = null
                                bitmap.value = BarcodeGeneratorAndSaver().generateBarCode(
                                    number,
                                    codeType.intValue
                                )
                                savePath.value = BarcodeGeneratorAndSaver().saveBitmapToFile(
                                    context = context,
                                    bitmap.value!!,
                                    id
                                )
                            }
                            if (savePath.value == null) {
                                validationMessage += "Failed to save barcode.\n"
                            }
                            if (bitmap.value == null) {
                                validationMessage += "Failed to generate barcode."
                            }
                            showValidationMessage = validationMessage.isNotEmpty()
                            if (showValidationMessage) {
                                return@Button
                            }
                            val card = Card(
                                id = id,
                                number = number,
                                name = name,
                                nameOfCard = nameOfCard,
                                codeType = codeType.intValue,
                                picture = savePath.value.toString(),
                                color = color.toHexString()
                            )
                            viewModel.addCardAndSave(card, onButtonClick)
                        }
                    ) {
                        Text(text = if (card != null) "Update Card" else "Add Card")
                    }
                    Button(
                        onClick = {
                            onCancelClick()
                        },
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Cancel"
                        )
                    }
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
            if (showColorPicker) {
                val initialColor = remember { color }
                val pickerInitialColor = remember {
                    if (colors.contains(initialColor)) {
                        Color.White
                    } else {
                        initialColor
                    }
                }
                Dialog(
                    onDismissRequest = {
                        color = initialColor
                        showColorPicker = false
                    }
                ) {
                    androidx.compose.material3.Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    ) {

                        val controller = rememberColorPickerController()
                        LaunchedEffect(controller) {
                            controller.selectByColor(pickerInitialColor, false)
                        }

                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                HsvColorPicker(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .padding(10.dp),
                                    controller = controller,
                                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                                        color = colorEnvelope.color
                                    }
                                )
                            }
                            item {
                                BrightnessSlider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .height(35.dp),
                                    controller = controller,
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = { showColorPicker = false },
                                        modifier = Modifier.padding(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = color
                                        )
                                    ) {
                                        val textColor =
                                            if (color.luminance() > 0.5) Color.Black else Color.White
                                        Text(
                                            text = "Select Color",
                                            color = textColor
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            showColorPicker = false
                                            color = initialColor
                                        },
                                        modifier = Modifier.padding(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = color
                                        )
                                    ) {
                                        val textColor =
                                            if (color.luminance() > 0.5) Color.Black else Color.White
                                        Text(
                                            text = "Cancel",
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ColorCircle(
        color: Color,
        isSelected: Boolean = false,
        isCustomPicker: Boolean = false,
        onClick: () -> Unit = {},
    ) {
        val rainbowBrush = Brush.sweepGradient(
            listOf(
                Color.Red,
                Color.Magenta,
                Color.Blue,
                Color.Cyan,
                Color.Green,
                Color.Yellow,
                Color.Red
            )
        )
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isCustomPicker && isSelected) SolidColor(color) else if (isCustomPicker) rainbowBrush else SolidColor(
                        color
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { onClick() }
            ) {
                val iconColor =
                    if (color.luminance() > 0.5) Color.Black else Color.White
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    fun Color.toHexString(): String {
        val red = (this.red * 255).toInt()
        val green = (this.green * 255).toInt()
        val blue = (this.blue * 255).toInt()
        return String.format("#%02X%02X%02X", red, green, blue)
    }

    @Composable
    fun ScannerOverlay(modifier: Modifier = Modifier) {
        val infiniteTransition = rememberInfiniteTransition(label = "scanner_line_transition")
        val scannerLinePosition by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scanner_line_position"
        )

        Canvas(modifier = modifier) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val cornerLength = 80f // The length of each corner line
            val cornerStrokeWidth = 10f // The thickness of the corner lines

            val boxSize = canvasWidth * 0.7f
            val boxLeft = (canvasWidth - boxSize) / 2
            val boxTop = (canvasHeight - boxSize) / 2
            val boxRight = boxLeft + boxSize
            val boxBottom = boxTop + boxSize

            val cornerColor = Color.White

            // Draw the animated scanning line
            val scannerLineY = boxTop + (boxSize * scannerLinePosition)
            drawLine(
                color = Color.Red.copy(alpha = 0.8f),
                start = Offset(boxLeft, scannerLineY),
                end = Offset(boxRight, scannerLineY),
                strokeWidth = 5f
            )

            // --- Draw corners using drawPath
            // Top-left corner path
            drawPath(
                path = Path().apply {
                    moveTo(boxLeft, boxTop + cornerLength)
                    lineTo(boxLeft, boxTop)
                    lineTo(boxLeft + cornerLength, boxTop)
                },
                color = cornerColor,
                style = Stroke(width = cornerStrokeWidth)
            )

            // Top-right corner path
            drawPath(
                path = Path().apply {
                    moveTo(boxRight - cornerLength, boxTop)
                    lineTo(boxRight, boxTop)
                    lineTo(boxRight, boxTop + cornerLength)
                },
                color = cornerColor,
                style = Stroke(width = cornerStrokeWidth)
            )

            // Bottom-left corner path
            drawPath(
                path = Path().apply {
                    moveTo(boxLeft, boxBottom - cornerLength)
                    lineTo(boxLeft, boxBottom)
                    lineTo(boxLeft + cornerLength, boxBottom)
                },
                color = cornerColor,
                style = Stroke(width = cornerStrokeWidth)
            )

            // Bottom-right corner path
            drawPath(
                path = Path().apply {
                    moveTo(boxRight - cornerLength, boxBottom)
                    lineTo(boxRight, boxBottom)
                    lineTo(boxRight, boxBottom - cornerLength)
                },
                color = cornerColor,
                style = Stroke(width = cornerStrokeWidth)
            )
        }
    }
}