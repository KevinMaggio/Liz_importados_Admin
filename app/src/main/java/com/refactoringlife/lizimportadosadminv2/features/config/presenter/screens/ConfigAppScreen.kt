package com.refactoringlife.lizimportadosadminv2.features.config.presenter.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyActionButton
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ConfigRequest
import com.refactoringlife.lizimportadosadminv2.core.network.service.ConfigService
import com.refactoringlife.lizimportadosadminv2.core.utils.uploadImageToStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigAppScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configService = remember { ConfigService() }
    
    // Estados para los switches
    var isOffersEnabled by remember { mutableStateOf(false) }
    var isCombosEnabled by remember { mutableStateOf(false) }
    
    // Estados para las opciones
    var options by remember { mutableStateOf(List(4) { index ->
        ConfigRequest.Option(
            name = "Opción ${index + 1}",
            image = ""
        )
    }) }
    var selectedImageUris by remember { mutableStateOf(List(4) { null as Uri? }) }
    var uploadedImageUrls by remember { mutableStateOf(List(4) { "" }) }
    
    // Estado de carga
    var isLoading by remember { mutableStateOf(false) }
    var currentImageIndex by remember { mutableStateOf(-1) }
    
    // Cargar configuración existente
    LaunchedEffect(Unit) {
        configService.getConfig().onSuccess { config ->
            isOffersEnabled = config.isOffers
            isCombosEnabled = config.combos
            if (config.circleOptions.isNotEmpty()) {
                // Convertir ConfigResponse.Option a ConfigRequest.Option
                val requestOptions = config.circleOptions.take(4).map { responseOption ->
                    ConfigRequest.Option(
                        name = responseOption.name,
                        image = responseOption.image
                    )
                }.toMutableList()
                
                // Asegurar que siempre tengamos 4 opciones
                while (requestOptions.size < 4) {
                    requestOptions.add(ConfigRequest.Option(name = "Opción ${requestOptions.size + 1}", image = ""))
                }
                
                options = requestOptions
                uploadedImageUrls = requestOptions.map { it.image }
            }
        }
    }
    
    // Launcher para seleccionar imágenes
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isLoading = true
                try {
                    val imageUrl = uploadImageToStorage(context, selectedUri)
                    if (imageUrl != null && currentImageIndex >= 0) {
                        uploadedImageUrls = uploadedImageUrls.toMutableList().apply {
                            this[currentImageIndex] = imageUrl
                        }
                        options = options.toMutableList().apply {
                            this[currentImageIndex] = this[currentImageIndex].copy(image = imageUrl)
                        }
                        selectedImageUris = selectedImageUris.toMutableList().apply {
                            this[currentImageIndex] = selectedUri
                        }
                    }
                } catch (e: Exception) {
                    // Manejar error
                } finally {
                    isLoading = false
                    currentImageIndex = -1
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar App") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Configuración de Secciones",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Sección de Ofertas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Sección de Ofertas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Habilitar ofertas")
                            Switch(
                                checked = isOffersEnabled,
                                onCheckedChange = { isOffersEnabled = it }
                            )
                        }
                    }
                }
                
                // Sección de Opciones
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Opciones Circulares (4 opciones)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        options.forEachIndexed { index, option ->
                            OptionItem(
                                option = option,
                                imageUri = selectedImageUris[index],
                                uploadedImageUrl = uploadedImageUrls[index],
                                onTitleChange = { newTitle ->
                                    options = options.toMutableList().apply {
                                        this[index] = this[index].copy(name = newTitle)
                                    }
                                },
                                onImageSelect = {
                                    currentImageIndex = index
                                    imageLauncher.launch("image/*")
                                },
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                
                // Sección de Combos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Sección de Combos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Habilitar combos")
                            Switch(
                                checked = isCombosEnabled,
                                onCheckedChange = { isCombosEnabled = it }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón de guardar
                LipsyActionButton(
                    text = if (isLoading) "Guardando..." else "Guardar Configuración",
                    icon = Icons.Default.ArrowBack,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val configRequest = ConfigRequest(
                                    isOffers = isOffersEnabled,
                                    circleOptions = options,
                                    combos = isCombosEnabled
                                )
                                configService.saveConfig(configRequest)
                                onNavigateBack()
                            } catch (e: Exception) {
                                // Manejar error
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OptionItem(
    option: ConfigRequest.Option,
    imageUri: Uri?,
    uploadedImageUrl: String,
    onTitleChange: (String) -> Unit,
    onImageSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Opción",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Campo de título
            OutlinedTextField(
                value = option.name,
                onValueChange = onTitleChange,
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Imagen
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 8.dp)
                        )
                    }
                    uploadedImageUrl.isNotEmpty() -> {
                        AsyncImage(
                            model = uploadedImageUrl,
                            contentDescription = "Imagen subida",
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 8.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = "Seleccionar imagen",
                                tint = Color.Gray
                            )
                        }
                    }
                }
                
                TextButton(onClick = onImageSelect) {
                    Text("Seleccionar Imagen")
                }
            }
        }
    }
} 