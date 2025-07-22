package com.refactoringlife.lizimportadosadmin.core.utils

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class LightBackgroundRemover {
    companion object {
        private const val STRIP_HEIGHT = 100 // Procesar la imagen en franjas de 100 píxeles
        private const val TOLERANCE = 30 // Tolerancia para comparar con el fondo
    }

    suspend fun removeBackground(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        try {
            // 1. Obtener color de referencia del fondo (promedio de las esquinas)
            val backgroundColor = getBackgroundColor(bitmap)

            // 2. Crear bitmap resultado
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawColor(Color.WHITE)

            // 3. Procesar por franjas para evitar OOM
            val paint = Paint().apply {
                isAntiAlias = true
            }

            var y = 0
            while (y < bitmap.height) {
                val stripHeight = minOf(STRIP_HEIGHT, bitmap.height - y)
                processStrip(bitmap, result, y, stripHeight, backgroundColor, paint)
                y += stripHeight
            }

            result
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getBackgroundColor(bitmap: Bitmap): Triple<Int, Int, Int> {
        // Tomar muestras de las esquinas
        val topLeft = bitmap.getPixel(0, 0)
        val topRight = bitmap.getPixel(bitmap.width - 1, 0)
        val bottomLeft = bitmap.getPixel(0, bitmap.height - 1)
        val bottomRight = bitmap.getPixel(bitmap.width - 1, bitmap.height - 1)

        // Calcular promedio
        val avgR = (Color.red(topLeft) + Color.red(topRight) + 
                   Color.red(bottomLeft) + Color.red(bottomRight)) / 4
        val avgG = (Color.green(topLeft) + Color.green(topRight) + 
                   Color.green(bottomLeft) + Color.green(bottomRight)) / 4
        val avgB = (Color.blue(topLeft) + Color.blue(topRight) + 
                   Color.blue(bottomLeft) + Color.blue(bottomRight)) / 4

        return Triple(avgR, avgG, avgB)
    }

    private fun processStrip(
        source: Bitmap,
        dest: Bitmap,
        startY: Int,
        height: Int,
        backgroundColor: Triple<Int, Int, Int>,
        paint: Paint
    ) {
        // Crear un buffer pequeño para la franja actual
        val strip = IntArray(source.width * height)
        
        // Leer la franja de píxeles
        source.getPixels(
            strip,
            0,
            source.width,
            0,
            startY,
            source.width,
            height
        )

        // Procesar píxeles
        for (i in strip.indices) {
            val x = i % source.width
            val y = startY + (i / source.width)
            
            val pixel = strip[i]
            if (!isBackground(pixel, backgroundColor)) {
                // Si no es fondo, copiar el píxel
                paint.color = pixel
                dest.setPixel(x, y, pixel)
            }
        }
    }

    private fun isBackground(pixel: Int, backgroundColor: Triple<Int, Int, Int>): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)

        // Verificar si el color es similar al fondo
        return abs(r - backgroundColor.first) < TOLERANCE &&
               abs(g - backgroundColor.second) < TOLERANCE &&
               abs(b - backgroundColor.third) < TOLERANCE
    }
} 