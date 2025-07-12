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
        private const val MAX_IMAGE_SIZE = 1024 // Tamaño máximo para procesar
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
            
            // Decodificar la imagen con opciones para optimizar memoria
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // Calcular el factor de escala para mantener la imagen en un tamaño manejable
            val scale = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            
            // Decodificar la imagen con el tamaño optimizado
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            
            val inputStream2 = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            inputStream2?.close()
            
            if (originalBitmap == null) {
                return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))
            }
            
            Log.d(TAG, "📏 Imagen original: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Crear bitmap con canal alfa
            val processedBitmap = Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            
            // Procesar píxel por píxel de manera más eficiente
            val pixels = IntArray(originalBitmap.width * originalBitmap.height)
            originalBitmap.getPixels(pixels, 0, originalBitmap.width, 0, 0, originalBitmap.width, originalBitmap.height)
            
            for (i in pixels.indices) {
                val pixel = pixels[i]
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                
                // Verificar si el píxel es "blanco" (cerca del blanco)
                val isWhite = red >= WHITE_THRESHOLD && 
                             green >= WHITE_THRESHOLD && 
                             blue >= WHITE_THRESHOLD
                
                pixels[i] = if (isWhite) {
                    // Hacer transparente
                    android.graphics.Color.TRANSPARENT
                } else {
                    // Mantener el color original
                    pixel
                }
            }
            
            processedBitmap.setPixels(pixels, 0, originalBitmap.width, 0, 0, originalBitmap.width, originalBitmap.height)
            
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
     * Calcula el factor de escala para optimizar el tamaño de la imagen
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
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
        
        Log.d(TAG, "💾 Imagen guardada: ${file.absolutePath}")
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