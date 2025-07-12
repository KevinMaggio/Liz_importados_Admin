package com.refactoringlife.lizimportadosadmin.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageProcessor {
    
    companion object {
        private const val TAG = "ImageProcessor"
        private const val WHITE_THRESHOLD = 240 // Umbral para considerar un píxel como "blanco"
        private const val TRANSPARENCY_THRESHOLD = 0.1f // Umbral de transparencia
    }
    
    /**
     * Procesa una imagen removiendo el fondo blanco
     */
    suspend fun removeWhiteBackground(
        context: Context,
        imageUri: Uri
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🖼️ Procesando imagen: $imageUri")
            
            // Decodificar la imagen
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))
            }
            
            // Crear bitmap con canal alfa
            val processedBitmap = Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            
            // Procesar píxel por píxel
            for (x in 0 until originalBitmap.width) {
                for (y in 0 until originalBitmap.height) {
                    val pixel = originalBitmap.getPixel(x, y)
                    
                    val red = android.graphics.Color.red(pixel)
                    val green = android.graphics.Color.green(pixel)
                    val blue = android.graphics.Color.blue(pixel)
                    
                    // Verificar si el píxel es "blanco" (cerca del blanco)
                    val isWhite = red >= WHITE_THRESHOLD && 
                                 green >= WHITE_THRESHOLD && 
                                 blue >= WHITE_THRESHOLD
                    
                    val newPixel = if (isWhite) {
                        // Hacer transparente
                        android.graphics.Color.TRANSPARENT
                    } else {
                        // Mantener el color original
                        pixel
                    }
                    
                    processedBitmap.setPixel(x, y, newPixel)
                }
            }
            
            // Guardar la imagen procesada
            val processedUri = saveProcessedImage(context, processedBitmap)
            
            // Limpiar memoria
            originalBitmap.recycle()
            processedBitmap.recycle()
            
            Log.d(TAG, "✅ Imagen procesada exitosamente")
            Result.success(processedUri)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error procesando imagen: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Guarda la imagen procesada en el almacenamiento interno
     */
    private fun saveProcessedImage(context: Context, bitmap: Bitmap): Uri {
        val fileName = "processed_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, fileName)
        
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
        
        return Uri.fromFile(file)
    }
    
    /**
     * Procesa múltiples imágenes
     */
    suspend fun processMultipleImages(
        context: Context,
        imageUris: List<Uri>
    ): List<Result<Uri>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔄 Procesando ${imageUris.size} imágenes")
        
        val results = mutableListOf<Result<Uri>>()
        
        imageUris.forEachIndexed { index, uri ->
            Log.d(TAG, "📸 Procesando imagen ${index + 1}/${imageUris.size}")
            val result = removeWhiteBackground(context, uri)
            results.add(result)
        }
        
        results
    }
} 