package com.refactoringlife.lizimportadosadminv2.features.home.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VentasBarChart(
    ventas: List<Pair<Date, Double>>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF2196F3),
    barWidth: Dp = 24.dp,
    maxBarHeight: Dp = 140.dp
) {
    if (ventas.isEmpty()) return
    val maxTotal = (ventas.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val animatedProgress by animateFloatAsState(targetValue = 1f, label = "AnimBar")

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(maxBarHeight + 32.dp)) {
            val barSpace = (size.width - (ventas.size * barWidth.toPx())) / (ventas.size + 1)
            ventas.forEachIndexed { idx, (fecha, total) ->
                val left = barSpace + idx * (barWidth.toPx() + barSpace)
                val barHeight = (total / maxTotal * maxBarHeight.toPx() * animatedProgress).toFloat()
                // Barra
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight - 24.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                // Valor encima de la barra
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        total.toInt().toString(),
                        left + barWidth.toPx() / 2,
                        size.height - barHeight - 28.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 28f
                            isFakeBoldText = true
                        }
                    )
                }
                // Etiqueta de fecha debajo
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        dateFormat.format(fecha),
                        left + barWidth.toPx() / 2,
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 24f
                        }
                    )
                }
            }
            // Eje Y (opcional)
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, size.height - 24.dp.toPx()),
                end = Offset(size.width, size.height - 24.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
} 