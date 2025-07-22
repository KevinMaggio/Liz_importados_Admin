package com.refactoringlife.lizimportadosadmin.core.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ImageProcessor {
    
    companion object {
        private const val TAG = "ImageProcessor"
        private const val MAX_IMAGE_SIZE = 1024 // Tamaño máximo para procesar
        
        // Umbrales más flexibles para la detección de fondo
        private const val LUMINOSITY_THRESHOLD = 200 // Umbral de luminosidad (0-255)
        private const val SATURATION_THRESHOLD = 30 // Umbral de saturación (0-255)
        private const val COLOR_DIFFERENCE_THRESHOLD = 30 // Diferencia máxima entre canales RGB
    }

    private fun isLikelyBackground(red: Int, green: Int, blue: Int): Boolean {
        // Calcular luminosidad (promedio simple RGB)
        val luminosity = (red + green + blue) / 3
        
        // Calcular saturación (diferencia entre max y min RGB)
        val maxRGB = max(max(red, green), blue)
        val minRGB = min(min(red, green), blue)
        val saturation = maxRGB - minRGB
        
        // Calcular diferencias entre canales
        val redGreenDiff = abs(red - green)
        val redBlueDiff = abs(red - blue)
        val greenBlueDiff = abs(green - blue)
        val maxDifference = max(max(redGreenDiff, redBlueDiff), greenBlueDiff)
        
        // Un píxel es considerado fondo si:
        // 1. Es suficientemente brillante (alta luminosidad)
        // 2. Tiene baja saturación (cerca del gris/blanco)
        // 3. Los canales RGB son similares (indica gris/blanco)
        return luminosity >= LUMINOSITY_THRESHOLD && 
               saturation <= SATURATION_THRESHOLD &&
               maxDifference <= COLOR_DIFFERENCE_THRESHOLD
    }
    
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
            var originalBitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            inputStream2?.close()
            
            if (originalBitmap == null) {
                return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))
            }
            
            // Leer orientación EXIF y rotar si es necesario
            val exifInputStream = context.contentResolver.openInputStream(imageUri)
            val exif = exifInputStream?.let { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifInputStream?.close()
            originalBitmap = orientation?.let { rotateBitmapIfNeeded(originalBitmap!!, it) } ?: originalBitmap
            
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
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                
                pixels[i] = if (isLikelyBackground(red, green, blue)) {
                    Color.TRANSPARENT
                } else {
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

    // Agregar funciones auxiliares para rotar el bitmap
    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
} 