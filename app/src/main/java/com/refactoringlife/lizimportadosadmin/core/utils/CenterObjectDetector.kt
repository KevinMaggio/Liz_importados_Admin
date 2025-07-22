package com.refactoringlife.lizimportadosadmin.core.utils

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class CenterObjectDetector {
    companion object {
        private const val EDGE_THRESHOLD = 25
        private const val BRIGHTNESS_THRESHOLD = 30
    }

    suspend fun removeBackground(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        // 1. Crear bitmap resultado
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        // 2. Encontrar punto de inicio en el centro
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val centerColor = bitmap.getPixel(centerX, centerY)
        
        // 3. Crear máscara para el flood fill
        val mask = Array(bitmap.height) { BooleanArray(bitmap.width) }
        
        // 4. Flood fill desde el centro
        floodFillFromCenter(bitmap, mask, centerX, centerY, centerColor)
        
        // 5. Expandir la selección para incluir bordes
        expandSelection(bitmap, mask)
        
        // 6. Aplicar la máscara con suavizado
        applyMaskWithSmoothing(bitmap, result, mask)
        
        result
    }

    private fun floodFillFromCenter(
        bitmap: Bitmap,
        mask: Array<BooleanArray>,
        startX: Int,
        startY: Int,
        targetColor: Int
    ) {
        val width = bitmap.width
        val height = bitmap.height
        val queue = ArrayDeque<Pair<Int, Int>>()
        
        // Iniciar desde el centro
        queue.add(startX to startY)
        mask[startY][startX] = true
        
        val visited = Array(height) { BooleanArray(width) }
        visited[startY][startX] = true
        
        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()
            val currentColor = bitmap.getPixel(x, y)
            
            // Verificar los 8 vecinos
            val neighbors = listOf(
                x to y - 1,     // arriba
                x to y + 1,     // abajo
                x - 1 to y,     // izquierda
                x + 1 to y,     // derecha
                x - 1 to y - 1, // diagonal superior izquierda
                x + 1 to y - 1, // diagonal superior derecha
                x - 1 to y + 1, // diagonal inferior izquierda
                x + 1 to y + 1  // diagonal inferior derecha
            )
            
            for ((nx, ny) in neighbors) {
                if (nx in 0 until width && ny in 0 until height && !visited[ny][nx]) {
                    val neighborColor = bitmap.getPixel(nx, ny)
                    if (isSimilarColor(neighborColor, targetColor)) {
                        queue.add(nx to ny)
                        mask[ny][nx] = true
                        visited[ny][nx] = true
                    }
                }
            }
        }
    }

    private fun expandSelection(bitmap: Bitmap, mask: Array<BooleanArray>) {
        val width = bitmap.width
        val height = bitmap.height
        val expanded = Array(height) { BooleanArray(width) }
        
        // Copiar máscara original
        for (y in 0 until height) {
            mask[y].copyInto(expanded[y])
        }
        
        // Expandir la selección
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                if (mask[y][x]) {
                    // Verificar vecinos
                    for (dy in -1..1) {
                        for (dx in -1..1) {
                            val nx = x + dx
                            val ny = y + dy
                            
                            if (!mask[ny][nx]) {
                                val centerColor = bitmap.getPixel(x, y)
                                val neighborColor = bitmap.getPixel(nx, ny)
                                
                                if (isEdge(centerColor, neighborColor)) {
                                    expanded[ny][nx] = true
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Actualizar máscara original
        for (y in 0 until height) {
            expanded[y].copyInto(mask[y])
        }
    }

    private fun applyMaskWithSmoothing(bitmap: Bitmap, result: Bitmap, mask: Array<BooleanArray>) {
        val canvas = Canvas(result)
        val paint = Paint().apply {
            isAntiAlias = true
            maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
        }
        
        // Crear path para el objeto
        val path = Path()
        
        // Encontrar contornos
        for (y in 0 until bitmap.height) {
            var inObject = false
            var start = 0
            
            for (x in 0 until bitmap.width) {
                if (mask[y][x] && !inObject) {
                    // Inicio de segmento
                    inObject = true
                    start = x
                } else if (!mask[y][x] && inObject) {
                    // Fin de segmento
                    inObject = false
                    path.addRect(
                        start.toFloat(),
                        y.toFloat(),
                        x.toFloat(),
                        (y + 1).toFloat(),
                        Path.Direction.CW
                    )
                }
            }
            
            // Si termina la línea dentro del objeto
            if (inObject) {
                path.addRect(
                    start.toFloat(),
                    y.toFloat(),
                    bitmap.width.toFloat(),
                    (y + 1).toFloat(),
                    Path.Direction.CW
                )
            }
        }
        
        // Dibujar el objeto
        canvas.drawBitmap(bitmap, 0f, 0f, paint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        })
        
        // Suavizar bordes
        canvas.drawPath(path, paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            xfermode = null
        })
    }

    private fun isSimilarColor(color1: Int, color2: Int): Boolean {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        val brightness1 = (r1 + g1 + b1) / 3
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        val brightness2 = (r2 + g2 + b2) / 3
        
        return abs(brightness1 - brightness2) < BRIGHTNESS_THRESHOLD
    }

    private fun isEdge(color1: Int, color2: Int): Boolean {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        val brightness1 = (r1 + g1 + b1) / 3
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        val brightness2 = (r2 + g2 + b2) / 3
        
        return abs(brightness1 - brightness2) > EDGE_THRESHOLD
    }
} 