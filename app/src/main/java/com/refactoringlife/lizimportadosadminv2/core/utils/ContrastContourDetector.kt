package com.refactoringlife.lizimportadosadminv2.core.utils

import android.graphics.*
import android.util.Log
import kotlin.math.abs

class ContrastContourDetector {
    companion object {
        private const val TAG = "ContrastContourDetector"
        private const val MIN_CONSECUTIVE_PIXELS = 10
        private const val CONTRAST_THRESHOLD = 30
    }

    data class ContourPoint(
        val x: Int,
        val y: Int,
        val isStart: Boolean // true si es inicio de objeto, false si es fin
    )

    fun detectContours(bitmap: Bitmap): Bitmap {
        val contourBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contourBitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        // Analizar línea por línea
        for (y in 0 until bitmap.height) {
            var consecutiveLightPixels = 0
            var consecutiveDarkPixels = 0
            var lastBrightness = getBrightness(bitmap.getPixel(0, y))
            var contourStart: Int? = null

            for (x in 1 until bitmap.width) {
                val currentBrightness = getBrightness(bitmap.getPixel(x, y))
                val brightnessDiff = abs(currentBrightness - lastBrightness)

                when {
                    // Detectar inicio de objeto (transición claro a oscuro)
                    brightnessDiff > CONTRAST_THRESHOLD && currentBrightness < lastBrightness -> {
                        consecutiveDarkPixels++
                        consecutiveLightPixels = 0
                        
                        if (consecutiveDarkPixels >= MIN_CONSECUTIVE_PIXELS && contourStart == null) {
                            contourStart = x - MIN_CONSECUTIVE_PIXELS
                            Log.d(TAG, "🎯 Inicio de objeto encontrado en y=$y, x=$contourStart")
                            // Marcar punto de inicio
                            canvas.drawPoint(contourStart.toFloat(), y.toFloat(), paint)
                        }
                    }
                    // Detectar fin de objeto (transición oscuro a claro)
                    brightnessDiff > CONTRAST_THRESHOLD && currentBrightness > lastBrightness -> {
                        consecutiveLightPixels++
                        consecutiveDarkPixels = 0
                        
                        if (consecutiveLightPixels >= MIN_CONSECUTIVE_PIXELS && contourStart != null) {
                            Log.d(TAG, "🎯 Fin de objeto encontrado en y=$y, x=$x")
                            // Dibujar línea desde el inicio hasta aquí
                            canvas.drawLine(
                                contourStart.toFloat(), y.toFloat(),
                                x.toFloat(), y.toFloat(),
                                paint
                            )
                            contourStart = null
                        }
                    }
                    else -> {
                        // Reiniciar contadores si no hay cambio significativo
                        if (brightnessDiff <= CONTRAST_THRESHOLD) {
                            consecutiveLightPixels = 0
                            consecutiveDarkPixels = 0
                        }
                    }
                }

                lastBrightness = currentBrightness
            }

            // Si llegamos al final de la línea y hay un objeto sin cerrar, cerrarlo
            if (contourStart != null) {
                canvas.drawLine(
                    contourStart.toFloat(), y.toFloat(),
                    bitmap.width.toFloat(), y.toFloat(),
                    paint
                )
            }
        }

        // Análisis vertical para conectar líneas
        connectVerticalLines(canvas, paint, bitmap.width, bitmap.height)

        return contourBitmap
    }

    private fun connectVerticalLines(canvas: Canvas, paint: Paint, width: Int, height: Int) {
        val verticalConnections = mutableListOf<Pair<PointF, PointF>>()

        // Buscar puntos finales cercanos en líneas adyacentes
        for (x in 0 until width) {
            var lastEndPoint: PointF? = null

            for (y in 0 until height) {
                if (isBlackPixel(canvas, x, y)) {
                    if (lastEndPoint == null) {
                        lastEndPoint = PointF(x.toFloat(), y.toFloat())
                    } else {
                        // Si la distancia vertical es pequeña, conectar los puntos
                        val currentPoint = PointF(x.toFloat(), y.toFloat())
                        if (y - lastEndPoint.y < MIN_CONSECUTIVE_PIXELS) {
                            verticalConnections.add(lastEndPoint to currentPoint)
                        }
                        lastEndPoint = currentPoint
                    }
                }
            }
        }

        // Dibujar las conexiones verticales
        verticalConnections.forEach { (start, end) ->
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        }
    }

    private fun getBrightness(color: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        // Fórmula de luminosidad percibida
        return (red * 0.299 + green * 0.587 + blue * 0.114).toInt()
    }

    private fun isBlackPixel(canvas: Canvas, x: Int, y: Int): Boolean {
        val bitmap = (canvas.clipBounds?.let { rect ->
            Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
        })?.also { bmp ->
            canvas.setBitmap(bmp)
        } ?: return false

        val pixel = bitmap.getPixel(x, y)
        bitmap.recycle()
        return Color.red(pixel) < 128 && Color.green(pixel) < 128 && Color.blue(pixel) < 128
    }
} 