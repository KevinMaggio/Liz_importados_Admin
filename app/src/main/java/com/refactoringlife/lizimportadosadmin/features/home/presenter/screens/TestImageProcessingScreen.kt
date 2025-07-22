package com.refactoringlife.lizimportadosadmin.features.home.presenter.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.refactoringlife.lizimportadosadmin.core.utils.ImageProcessor
import kotlinx.coroutines.launch

@Composable
fun TestImageProcessingScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var processedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            isProcessing = true
            error = null
            coroutineScope.launch {
                try {
                    val processor = ImageProcessor()
                    val result = processor.removeWhiteBackground(context, uri)
                    if (result.isSuccess) {
                        processedImageUri = result.getOrNull()
                    } else {
                        error = result.exceptionOrNull()?.message
                    }
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isProcessing = false
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Prueba de Procesamiento de Imágenes",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Procesando..." else "Seleccionar Imagen")
        }
        
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        
        if (error != null) {
            Text(
                text = "Error: $error",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Imagen original
        if (selectedImageUri != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Imagen Original:")
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Imagen original",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        // Imagen procesada con fondo amarillo
        if (processedImageUri != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Imagen Procesada (fondo amarillo):")
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.Yellow)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = processedImageUri,
                    contentDescription = "Imagen procesada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
} 