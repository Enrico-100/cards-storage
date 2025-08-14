package com.example.cards_app



import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AddCard {
    @Composable
    fun MyAddCard(
        viewModel: MainViewModel,
        onButtonClick: () -> Unit = {}

    ) {
        val context = LocalContext.current
        val bitmap = remember { mutableStateOf<Bitmap?>(null) }
        val savePath = remember { mutableStateOf<String?>(null) }
        var number by remember { mutableIntStateOf(0) }
        var name by remember { mutableStateOf("") }
        var nameOfCard by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ){
            OutlinedTextField(
                value = number.toString(),
                onValueChange = {
                    number = it.toIntOrNull() ?: 0
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
            bitmap.value?.asImageBitmap()?.let { it ->
                Image(
                    bitmap = it,
                    contentDescription = "Generate BarCode Image",
                    modifier = Modifier.size(250.dp)
                )
            }
            Button(
                onClick = {
                    bitmap.value = generateBarCode(number.toString())
                    savePath.value = saveBitmapToFile(context = context, bitmap.value!!, "$nameOfCard-$name")
                    val card = Card(number, name, nameOfCard, savePath.value.toString())
                    onButtonClick()
                    viewModel.addCardAndSave(card)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Add Card")
            }
        }


    }
}
fun generateBarCode(text: String): Bitmap {
    val width = 1000
    val height = 300
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val codeWriter = MultiFormatWriter()
    try {
        val bitMatrix = codeWriter.encode(
            text,
            BarcodeFormat.CODE_128,
            width,
            height
        )
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (bitMatrix[x, y]) Color.BLACK else Color.TRANSPARENT
                bitmap[x, y] = color
            }
        }
    } catch (e: WriterException) {
        Log.d("TAG", "generateBarCode: ${e.message}")
    }
    return bitmap
}
fun saveBitmapToFile(
    context: Context,
    bitmap: Bitmap,
    desiredFileName: String
): String? {
    val imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    if (imageDir == null) {
        Log.e("SaveBitmap", "External files directory not available.")
        return null
    }
    if (!imageDir.exists()) {
        if (!imageDir.mkdirs()) {
            Log.e("SaveBitmap", "Failed to create directory: ${imageDir.absolutePath}")
            return null // Failed to create directory
        }
    }

    val finalFileName = "$desiredFileName.png"
    val imageFile = File(imageDir, finalFileName)
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        Log.i("SaveBitmap", "Image saved successfully: ${imageFile.absolutePath}")
        return imageFile.absolutePath
    } catch (e: IOException) {
        Log.e("SaveBitmap", "Error saving image: ${e.message}", e)
    } catch (e: Exception) { // Catch any other unexpected errors during saving
        Log.e("SaveBitmap", "Unexpected error saving image: ${e.message}", e)
    } finally {
        try {
            fos?.close()
        } catch (e: IOException) {
            Log.e("SaveBitmap", "Error closing FileOutputStream: ${e.message}", e)
        }
    }
    if (imageFile.exists()) {
        imageFile.delete()
    }
    return null
}