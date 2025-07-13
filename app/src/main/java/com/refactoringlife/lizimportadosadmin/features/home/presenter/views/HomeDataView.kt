package com.refactoringlife.lizimportadosadmin.features.home.presenter.views

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportadosadmin.features.home.composables.ProcessedImagesGallery
import com.refactoringlife.lizimportadosadmin.features.home.presenter.viewmodel.HomeUiState
import com.refactoringlife.lizimportadosadmin.ui.theme.ColorWhiteLipsy

@Composable
fun HomeDataView(
    modifier: Modifier = Modifier,
    uiState: HomeUiState = HomeUiState.Idle,
    processedImages: List<Uri> = emptyList(),
    onProcessImagesClick: () -> Unit = {},
    onClearImagesClick: () -> Unit = {},
    onNavigateToAddProduct: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhiteLipsy)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Panel de Administración",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Mostrar estado del procesamiento
            when (uiState) {
                is HomeUiState.Idle -> {
                    Button(
                        onClick = onProcessImagesClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(height = 56.dp, width = 200.dp)
                    ) {
                        Text("Procesar Imágenes")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onNavigateToAddProduct,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(height = 56.dp, width = 200.dp)
                    ) {
                        Text("Agregar Producto (Subir Imagen)")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Eliminar fondos de fotos de productos",
                        fontSize = 14.sp
                    )
                }
                
                is HomeUiState.SelectingImages -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Seleccionando imágenes...",
                        fontSize = 16.sp
                    )
                }
                
                is HomeUiState.ProcessingImages -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Procesando ${uiState.count} imágenes...",
                        fontSize = 16.sp
                    )
                }
                
                is HomeUiState.ProcessingComplete -> {
                    Text(
                        text = "✅ Procesamiento completado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${uiState.processedCount} imágenes procesadas exitosamente",
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onProcessImagesClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(height = 56.dp, width = 200.dp)
                    ) {
                        Text("Procesar Más Imágenes")
                    }
                }
                
                is HomeUiState.Error -> {
                    Text(
                        text = "❌ Error",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = uiState.message,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onProcessImagesClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(height = 56.dp, width = 200.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
            
            // Mostrar galería de imágenes procesadas
            if (processedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                
                ProcessedImagesGallery(
                    images = processedImages,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onClearImagesClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Limpiar Imágenes")
                }
            }
        }
    }
}