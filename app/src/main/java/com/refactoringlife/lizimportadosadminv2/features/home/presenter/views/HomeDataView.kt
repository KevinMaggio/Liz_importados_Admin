package com.refactoringlife.lizimportadosadminv2.features.home.presenter.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportadosadminv2.ui.theme.ColorWhiteLipsy
import androidx.compose.ui.graphics.Color
import com.refactoringlife.lizimportadosadminv2.features.home.composables.VentasBarChart
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.refactoringlife.lizimportadosadminv2.features.home.presenter.viewmodel.HomeViewModel
import com.refactoringlife.lizimportadosadminv2.features.home.presenter.viewmodel.HomeUiState

@Composable
fun HomeDataView(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToCarts: () -> Unit = {},
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToSelectProductForEdit: () -> Unit = {},
    onNavigateToCreateCombo: () -> Unit = {},
    onNavigateToManageCombos: () -> Unit = {},
    onNavigateToEditProductDetail: (String) -> Unit = {},
    onNavigateToDeleteProduct: () -> Unit = {},
    onNavigateToVenderProducto: () -> Unit = {},
    onNavigateToConfigApp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val mensajeAleatorio = remember { viewModel.getMensajeMotivacional() }
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
            
            when (uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HomeUiState.Error -> {
                    Text(
                        text = "Error: ${(uiState as HomeUiState.Error).message}",
                        color = Color.Red
                    )
                }
                is HomeUiState.Success -> {
                    val stats = (uiState as HomeUiState.Success).stats
                    
                    // Gráfico de ventas semanales
                    if (stats.ventasSemanales.isNotEmpty()) {
                        Text(
                            text = "Ventas de la semana",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        VentasBarChart(
                            ventas = stats.ventasSemanales,
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
                                Text(text = stats.productosActivos.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                                Text(text = stats.productosVendidos.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                                Text(text = stats.combosActivos.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                                Text(text = stats.combosVendidos.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "Combos vendidos", color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Alerta de bajo stock
                    if (stats.productosBajoStock.isNotEmpty()) {
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
                                stats.productosBajoStock.forEach { nombre ->
                                    Text(text = nombre, color = Color.Black)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Botones de acción modernos
            // Botón de Carritos (primero)
            LipsyActionButton(
                text = "Gestionar Carritos",
                icon = Icons.Default.ShoppingCart,
                onClick = onNavigateToCarts,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFF673AB7)
            )

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
                onClick = onNavigateToSelectProductForEdit,
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
                text = "Gestionar Combos",
                icon = Icons.Default.ShoppingCart,
                onClick = onNavigateToManageCombos,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFF9C27B0)
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
                onClick = onNavigateToVenderProducto,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                containerColor = Color(0xFF6D4C41)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Configuración
            LipsyActionButton(
                text = "Configurar Secciones de la App",
                icon = Icons.Default.Settings,
                onClick = onNavigateToConfigApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}