package com.refactoringlife.lizimportadosadminv2.core.utils

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImageOptimizer {
    companion object {
        private const val TAG = "ImageOptimizer"
        private const val MAX_WIDTH = 600 // Reducir ancho máximo para productos
        private const val TARGET_SIZE_KB = 150 // Aumentar tamaño objetivo (era 80)
        private const val QUALITY_START = 90 // Calidad inicial más alta (era 85)
        private const val QUALITY_MIN = 60 // Calidad mínima más alta (era 25)
    }

    data class OptimizationResult(
        val uri: Uri,
        val sizeKB: Long
    )

    suspend fun optimizeImage(context: Context, imageUri: Uri): Result<OptimizationResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🖼️ Optimizando imagen: $imageUri")
            
            // 1. Obtener orientación EXIF
            val orientation = getImageOrientation(context, imageUri)
            Log.d(TAG, "📏 Orientación EXIF detectada: $orientation")
            
            // 2. Cargar y rotar imagen si es necesario
            var bitmap = loadScaledBitmap(context, imageUri)
                ?: return@withContext Result.failure(Exception("No se pudo cargar la imagen"))
            
            // 3. Rotar si es necesario
            bitmap = rotateBitmapIfNeeded(bitmap, orientation)
            
            // 4. Asegurar orientación vertical
            bitmap = ensurePortraitOrientation(bitmap)
            
            // 5. Optimizar calidad/tamaño
            val optimizedBytes = optimizeImageQuality(bitmap)
            
            // 6. Guardar resultado
            val resultUri = saveOptimizedImage(context, optimizedBytes)
            
            // 7. Limpiar
            bitmap.recycle()
            
            // 8. Loggear resultados
            val finalSize = File(resultUri.path!!).length() / 1024
            Log.d(TAG, "✅ Imagen optimizada: $finalSize KB")
            
            Result.success(OptimizationResult(resultUri, finalSize))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error optimizando imagen: ${e.message}")
            Result.failure(e)
        }
    }

    private fun getImageOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ No se pudo leer orientación EXIF: ${e.message}")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        
        return try {
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true
            )
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error rotando imagen: ${e.message}")
            bitmap
        }
    }

    private fun ensurePortraitOrientation(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= bitmap.height) {
            return bitmap // Ya está en portrait o es cuadrada
        }
        
        // Rotar 90 grados para convertir a portrait
        val matrix = Matrix()
        matrix.postRotate(90f)
        
        return try {
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true
            )
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            Log.d(TAG, "🔄 Imagen rotada a portrait")
            rotatedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error convirtiendo a portrait: ${e.message}")
            bitmap
        }
    }

    private fun loadScaledBitmap(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            // Primero decodificar dimensiones
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            
            // Calcular escala necesaria
            val scale = calculateScaleFactor(options.outWidth, options.outHeight)
            
            // Recargar con escala
            context.contentResolver.openInputStream(uri)?.use { newInputStream ->
                BitmapFactory.Options().apply {
                    inSampleSize = scale
                    inPreferredConfig = Bitmap.Config.RGB_565 // Usar 16 bits por pixel
                }.let { scaledOptions ->
                    BitmapFactory.decodeStream(newInputStream, null, scaledOptions)
                }
            }
        }
    }

    private fun calculateScaleFactor(width: Int, height: Int): Int {
        var scale = 1
        val maxDimension = maxOf(width, height)
        
        while (maxDimension / scale > MAX_WIDTH) {
            scale *= 2
        }
        
        return scale
    }

    private fun optimizeImageQuality(bitmap: Bitmap): ByteArray {
        var quality = QUALITY_START
        var bestBytes: ByteArray? = null
        var shouldContinue = true

        while (shouldContinue && quality >= QUALITY_MIN) {
            ByteArrayOutputStream().use { outputStream ->
                // Usar WebP para mejor compresión
                bitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
                val bytes = outputStream.toByteArray()
                val sizeKB = bytes.size / 1024
                
                Log.d(TAG, "📊 Prueba con calidad $quality: $sizeKB KB (WebP)")
                
                if (sizeKB <= TARGET_SIZE_KB || quality <= QUALITY_MIN) {
                    bestBytes = bytes
                    shouldContinue = false
                    Log.d(TAG, "✅ Calidad final: $quality, Tamaño: $sizeKB KB (WebP)")
                } else {
                    // Reducir calidad gradualmente
                    quality -= 5
                }
            }
        }

        return bestBytes ?: ByteArrayOutputStream().also { 
            bitmap.compress(Bitmap.CompressFormat.WEBP, QUALITY_MIN, it)
        }.toByteArray()
    }

    private fun saveOptimizedImage(context: Context, imageBytes: ByteArray): Uri {
        val fileName = "optimized_${System.currentTimeMillis()}.webp"
        val file = File(context.filesDir, fileName)
        
        FileOutputStream(file).use { outputStream ->
            outputStream.write(imageBytes)
        }
        
        // Usar FileProvider para compatibilidad con Android 7+
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
} 