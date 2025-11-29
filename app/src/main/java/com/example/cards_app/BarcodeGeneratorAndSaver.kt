package com.example.cards_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BarcodeGeneratorAndSaver {
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
                    val color = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
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
        cardId: String
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
        val oldFiles = imageDir.listFiles { _, name ->
            name.startsWith(cardId) && name.endsWith(".png")
        }
        oldFiles?.forEach { it.delete() }

        val timestamp = System.currentTimeMillis().toString(16)
        val finalFileName = "${cardId}_${timestamp}.png"
        val imageFile = File(imageDir, finalFileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            Log.i("SaveBitmap", "Image saved successfully: ${imageFile.absolutePath}")
            return imageFile.absolutePath
        } catch (e: IOException) {
            Log.e("SaveBitmap", "Error saving image: ${e.message}", e)
            if (imageFile.exists()) {
                imageFile.delete()
            }
            return null
        } catch (e: Exception) { // Catch any other unexpected errors during saving
            Log.e("SaveBitmap", "Unexpected error saving image: ${e.message}", e)
            if (imageFile.exists()) {
                imageFile.delete()
            }
            return null
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                Log.e("SaveBitmap", "Error closing FileOutputStream: ${e.message}", e)
            }
        }
    }
}