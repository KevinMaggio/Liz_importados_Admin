package com.refactoringlife.lizimportadosadmin.features.home.presenter.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.refactoringlife.lizimportadosadmin.features.home.composables.VentasBarChart
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.refactoringlife.lizimportadosadmin.core.composablesLipsy.LipsyActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.width

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
        "Hoy puedes superar tus propias metas.",
        "Siempre recorda que no estas sola.",
        "Hay dias dificiles, pero vos podes con todo!",
        "Un dia nuevo, es una nueva oportunidad de comenzar!, vamos a romperla!!!",
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

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhiteLipsy)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensaje motivacional destacado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = mensajeAleatorio,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22223B),
                    letterSpacing = 1.2.sp
                )
            }
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
            // Botones de acción modernos
            LipsyActionButton(
                text = "Agregar Producto",
                icon = Icons.Default.Add,
                onClick = onNavigateToAddProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFF4CAF50)
            )
            LipsyActionButton(
                text = "Editar Producto",
                icon = Icons.Default.Edit,
                onClick = onNavigateToEditProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFF2196F3)
            )
            LipsyActionButton(
                text = "Crear Combo de Productos",
                icon = Icons.Default.ShoppingCart,
                onClick = onNavigateToCreateCombo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFFFF9800)
            )
            LipsyActionButton(
                text = "Eliminar Producto",
                icon = Icons.Default.Delete,
                onClick = onNavigateToDeleteProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFFF44336)
            )
            LipsyActionButton(
                text = "Vender Producto",
                icon = Icons.Default.CheckCircle,
                onClick = onNavigateToVenderProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFF6D4C41)
            )
        }
    }
}