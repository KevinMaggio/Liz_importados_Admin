package com.refactoringlife.lizimportadosadminv2.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageProcessor {
    companion object {
        private const val TAG = "ImageProcessor"
        
        suspend fun processImage(context: Context, imageUri: Uri): Result<ImageOptimizer.OptimizationResult> = withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🖼️ Procesando imagen: $imageUri")
                
                val optimizer = ImageOptimizer()
                optimizer.optimizeImage(context, imageUri)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error procesando imagen: ${e.message}")
                Result.failure(e)
            }
        }
    }
} 