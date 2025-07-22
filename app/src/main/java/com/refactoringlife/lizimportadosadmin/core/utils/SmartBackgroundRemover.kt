package com.refactoringlife.lizimportadosadmin.core.utils

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

class SmartBackgroundRemover {
    companion object {
        private const val EDGE_THRESHOLD = 25
        private const val BACKGROUND_TOLERANCE = 30
        private const val SAMPLE_SIZE = 10
    }

    suspend fun removeBackground(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        // Convertir bitmap a array para procesamiento más rápido
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        // 1. Analizar el fondo
        val avgBackground = sampleBackgroundColors(pixels, bitmap.width, bitmap.height)
        
        // 2. Crear máscara
        val mask = createMask(pixels, bitmap.width, bitmap.height, avgBackground)
        
        // 3. Aplicar máscara y crear resultado
        createResult(bitmap, mask)
    }

    private fun sampleBackgroundColors(pixels: IntArray, width: Int, height: Int): Triple<Int, Int, Int> {
        var sumR = 0
        var sumG = 0
        var sumB = 0
        var count = 0
        
        // Muestrear bordes
        val stepX = width / SAMPLE_SIZE
        val stepY = height / SAMPLE_SIZE
        
        // Borde superior e inferior
        for (x in 0 until width step stepX) {
            // Superior
            val topPixel = pixels[x]
            sumR += Color.red(topPixel)
            sumG += Color.green(topPixel)
            sumB += Color.blue(topPixel)
            
            // Inferior
            val bottomPixel = pixels[(height - 1) * width + x]
            sumR += Color.red(bottomPixel)
            sumG += Color.green(bottomPixel)
            sumB += Color.blue(bottomPixel)
            
            count += 2
        }
        
        // Bordes laterales
        for (y in 0 until height step stepY) {
            // Izquierdo
            val leftPixel = pixels[y * width]
            sumR += Color.red(leftPixel)
            sumG += Color.green(leftPixel)
            sumB += Color.blue(leftPixel)
            
            // Derecho
            val rightPixel = pixels[y * width + width - 1]
            sumR += Color.red(rightPixel)
            sumG += Color.green(rightPixel)
            sumB += Color.blue(rightPixel)
            
            count += 2
        }
        
        return Triple(sumR / count, sumG / count, sumB / count)
    }

    private suspend fun createMask(
        pixels: IntArray,
        width: Int,
        height: Int,
        avgBackground: Triple<Int, Int, Int>
    ): ByteArray = withContext(Dispatchers.Default) {
        val mask = ByteArray(width * height)
        val gradients = FloatArray(width * height)
        
        // 1. Calcular gradientes
        calculateGradients(pixels, width, height, gradients)
        
        // 2. Marcar píxeles basados en gradiente y color de fondo
        for (i in pixels.indices) {
            val isEdge = gradients[i] > EDGE_THRESHOLD
            val isBackground = isBackgroundColor(pixels[i], avgBackground)
            
            mask[i] = when {
                isEdge -> 1
                !isBackground -> 1
                else -> 0
            }
        }
        
        // 3. Flood fill desde los bordes
        floodFillFromEdges(mask, width, height)
        
        mask
    }

    private fun calculateGradients(pixels: IntArray, width: Int, height: Int, gradients: FloatArray) {
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val i = y * width + x
                
                // Calcular diferencias con vecinos
                val center = getIntensity(pixels[i])
                val left = getIntensity(pixels[i - 1])
                val right = getIntensity(pixels[i + 1])
                val top = getIntensity(pixels[i - width])
                val bottom = getIntensity(pixels[i + width])
                
                // Gradiente simplificado
                val gx = right - left
                val gy = bottom - top
                
                gradients[i] = sqrt((gx * gx + gy * gy).toFloat())
            }
        }
    }

    private fun getIntensity(pixel: Int): Int {
        return (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
    }

    private fun floodFillFromEdges(mask: ByteArray, width: Int, height: Int) {
        val queue = ArrayDeque<Int>()
        
        // Agregar bordes a la cola
        for (x in 0 until width) {
            if (mask[x] == 0.toByte()) queue.add(x)
            if (mask[(height - 1) * width + x] == 0.toByte()) queue.add((height - 1) * width + x)
        }
        for (y in 0 until height) {
            if (mask[y * width] == 0.toByte()) queue.add(y * width)
            if (mask[y * width + width - 1] == 0.toByte()) queue.add(y * width + width - 1)
        }
        
        while (queue.isNotEmpty()) {
            val i = queue.removeFirst()
            val x = i % width
            val y = i / width
            
            // Verificar vecinos
            val neighbors = listOf(
                i - width, // arriba
                i + width, // abajo
                i - 1,    // izquierda
                i + 1     // derecha
            )
            
            for (ni in neighbors) {
                if (ni in mask.indices &&
                    abs(ni % width - x) <= 1 && // Evitar wrap-around horizontal
                    mask[ni] == 0.toByte()
                ) {
                    mask[ni] = 0
                    queue.add(ni)
                }
            }
        }
    }

    private suspend fun createResult(bitmap: Bitmap, mask: ByteArray): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        // Dibujar fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Aplicar máscara con suavizado
        val paint = Paint().apply {
            isAntiAlias = true
            maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        }
        
        // Copiar píxeles del objeto
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        for (i in pixels.indices) {
            if (mask[i] == 1.toByte()) {
                val x = i % bitmap.width
                val y = i / bitmap.width
                canvas.drawPoint(x.toFloat(), y.toFloat(), paint.apply { color = pixels[i] })
            }
        }
        
        result
    }

    private fun isBackgroundColor(pixel: Int, avgBackground: Triple<Int, Int, Int>): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        
        return abs(r - avgBackground.first) < BACKGROUND_TOLERANCE &&
               abs(g - avgBackground.second) < BACKGROUND_TOLERANCE &&
               abs(b - avgBackground.third) < BACKGROUND_TOLERANCE
    }
} 