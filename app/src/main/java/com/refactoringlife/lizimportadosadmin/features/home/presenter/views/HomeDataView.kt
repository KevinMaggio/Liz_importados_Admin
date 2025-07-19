package com.refactoringlife.lizimportadosadmin.features.home.presenter.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportadosadmin.ui.theme.ColorWhiteLipsy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.refactoringlife.lizimportadosadmin.core.network.fireStore.FireStoreStats
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.refactoringlife.lizimportadosadmin.features.home.composables.VentasBarChart

@Composable
fun HomeDataView(
    modifier: Modifier = Modifier,
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToEditProduct: () -> Unit = {},
    onNavigateToCreateCombo: () -> Unit = {},
    onNavigateToDeleteProduct: () -> Unit = {},
    onNavigateToVenderProduct: () -> Unit = {}
) {
    // Lista de mensajes motivacionales
    val mensajesMotivacionales = listOf(
        "¡Hoy es un gran día para lograr nuevas ventas!",
        "Recuerda: cada producto vendido es un cliente feliz.",
        "¡Sigue así! Tu esfuerzo se refleja en los resultados.",
        "La constancia es la clave del éxito.",
        "¡Vamos por más! Cada día es una nueva oportunidad.",
        "El éxito es la suma de pequeños esfuerzos repetidos día tras día.",
        "¡Tu dedicación mueve este negocio!",
        "No te detengas, los grandes logros requieren tiempo.",
        "¡Eres el motor de Liz Importados!",
        "Hoy puedes superar tus propias metas."
    )
    // Seleccionar mensaje aleatorio
    val mensajeAleatorio = remember { mensajesMotivacionales.random() }

    // Estados para datos
    val scope = rememberCoroutineScope()
    var ventasSemanales by remember { mutableStateOf<List<Pair<java.util.Date, Double>>>(emptyList()) }
    var metricasProductos by remember { mutableStateOf(0 to 0) }
    var metricasCombos by remember { mutableStateOf(0 to 0) }
    var productosBajoStock by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                loading = true
                ventasSemanales = FireStoreStats.getVentasSemanales()
                metricasProductos = FireStoreStats.getMetricasProductos()
                metricasCombos = FireStoreStats.getMetricasCombos()
                productosBajoStock = FireStoreStats.getProductosBajoStock()
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhiteLipsy)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensaje motivacional
            Text(
                text = mensajeAleatorio,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            if (loading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text("Error: $error", color = Color.Red)
            } else {
                // Gráfico de ventas semanales
                if (ventasSemanales.isNotEmpty()) {
                    Text(
                        text = "Ventas de la semana",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    VentasBarChart(
                        ventas = ventasSemanales,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(bottom = 24.dp)
                    )
                }
                // Tarjetas de métricas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = metricasProductos.first.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Productos activos", color = Color.White)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = metricasProductos.second.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Productos vendidos", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = metricasCombos.first.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Combos activos", color = Color.White)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = metricasCombos.second.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Combos vendidos", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Alerta de bajo stock
                if (productosBajoStock.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Atención! Productos con bajo stock:",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            productosBajoStock.forEach { nombre ->
                                Text(text = nombre, color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            // Botones de acción (mantengo los existentes)
            Button(
                onClick = onNavigateToAddProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Agregar Producto")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToEditProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Editar Producto")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToCreateCombo,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Crear Combo de Productos")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToDeleteProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Eliminar Producto")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToVenderProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Vender Producto")
            }
        }
    }
}