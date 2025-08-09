package com.refactoringlife.lizimportadosadminv2.features.addProduct.presenter.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ProductRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.refactoringlife.lizimportadosadminv2.core.utils.ImageProcessor
import com.refactoringlife.lizimportadosadminv2.core.utils.uploadImageToStorage
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import com.refactoringlife.lizimportadosadminv2.core.utils.ProductConstants
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyDropdown
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyMultiSelect
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import android.util.Log
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.tasks.await
import com.refactoringlife.lizimportadosadminv2.core.repository.ProductRepository

@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var processing by remember { mutableStateOf(false) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadChecks by remember { mutableStateOf<List<Boolean>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    // Verificar autenticación
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    // Campos del formulario
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var gender by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // Agregar estado para tamaños de imágenes
    val imageSizes = remember { mutableStateMapOf<Uri, Long>() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // Verificar autenticación antes de procesar
            if (currentUser == null) {
                Log.e("AddProductScreen", "❌ Usuario no autenticado")
                error = "Debes iniciar sesión para subir imágenes"
                return@rememberLauncherForActivityResult
            }
            
            Log.d("AddProductScreen", "✅ Usuario autenticado: ${currentUser.email}")
            processing = true
            selectedUris = uris
            uploadChecks = List(uris.size) { false }
            uploadedUrls = emptyList()
            coroutineScope.launch {
                val urls = mutableListOf<String>()
                val checks = mutableListOf<Boolean>()
                for ((index, uri) in uris.withIndex()) {
                    try {
                        Log.d("AddProductScreen", "🖼️ Procesando imagen ${index + 1}/${uris.size}: $uri")
                        
                        // Procesar imagen
                        val result = ImageProcessor.processImage(context, uri)
                        if (result.isSuccess) {
                            val optimizedResult = result.getOrNull()!!
                            Log.d("AddProductScreen", "✅ Imagen optimizada: ${optimizedResult.sizeKB} KB")
                            
                            // Guardar tamaño para mostrar
                            imageSizes[uri] = optimizedResult.sizeKB
                            
                            // Subir a Storage
                            val imageUrl = uploadImageToStorage(context = context,
                                imageUri = optimizedResult.uri)
                            if (imageUrl != null) {
                                Log.d("AddProductScreen", "✅ Imagen subida: $imageUrl")
                                urls.add(imageUrl)
                                checks.add(true)
                            } else {
                                checks.add(false)
                                error = "Error subiendo imagen ${index + 1}"
                            }
                        } else {
                            checks.add(false)
                            error = "Error procesando imagen ${index + 1}: ${result.exceptionOrNull()?.message}"
                        }
                    } catch (e: Exception) {
                        Log.e("AddProductScreen", "❌ Error inesperado: ${e.message}")
                        checks.add(false)
                        error = "Error inesperado en imagen ${index + 1}: ${e.message}"
                    }
                }
                uploadedUrls = urls
                uploadChecks = checks
                processing = false
            }
        }
    }

    // Agregar ProductRepository
    val productRepository = remember { ProductRepository() }

    suspend fun saveProductToFirestore(product: ProductRequest): Result<Unit> {
        return productRepository.addProduct(product)
    }

    suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Agregar Producto",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !processing && currentUser != null
                ) {
                    Text("Seleccionar Imágenes")
                }
                
                if (currentUser == null) {
                    Text(
                        text = "Debes iniciar sesión para subir imágenes",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (processing) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
                if (selectedUris.isNotEmpty()) {
                    Text("Imágenes seleccionadas:", modifier = Modifier.padding(top = 16.dp))
                }
            }
            itemsIndexed(selectedUris) { idx, uri ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    Checkbox(checked = uploadChecks.getOrNull(idx) == true, onCheckedChange = null, enabled = false)
                    Text("Imagen ${idx + 1}")
                    if (uploadChecks.getOrNull(idx) == true) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Subida", tint = Color(0xFF388E3C), modifier = Modifier.size(20.dp).padding(start = 4.dp))
                    }
                    // Mostrar tamaño de imagen optimizada
                    imageSizes[uri]?.let { sizeKB ->
                        Text(
                            text = " (${sizeKB} KB)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                if (error != null) {
                    Text(text = "Error: $error", modifier = Modifier.padding(top = 16.dp), color = Color.Red)
                }
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Descripción - Múltiples líneas
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(120.dp), // Altura fija para descripción
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Marca - Una línea
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marca") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Talla - Una línea
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Talla") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Categorías múltiples
                LipsyMultiSelect(
                    label = "Categorías",
                    options = ProductConstants.CATEGORIES,
                    selectedOptions = selectedCategories,
                    onSelectionChanged = { selectedCategories = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LipsyDropdown(
                    label = "Género",
                    options = ProductConstants.GENDERS,
                    selectedOption = gender,
                    onOptionSelected = { gender = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Precio - Solo números enteros
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        // Solo permitir números
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            price = newValue
                        }
                    },
                    label = { Text("Precio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        processing = true
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            val product = ProductRequest(
                                id = UUID.randomUUID().toString(),
                                name = name.ifEmpty { null },
                                description = description.ifEmpty { null },
                                brand = brand.ifEmpty { null },
                                size = size.ifEmpty { null },
                                categories = if (selectedCategories.isNotEmpty()) selectedCategories else null,
                                comboIds = emptyList(),
                                gender = gender.ifEmpty { null },
                                images = uploadedUrls,
                                isAvailable = true, // Hardcodeado como true para nuevos productos
                                isOffer = false, // Hardcodeado como false
                                offerPrice = 0,
                                price = price.toIntOrNull()
                            )
                            val result = saveProductToFirestore(product)
                            if (result.isSuccess) {
                                success = product.id
                                showSuccess = true
                                snackbarHostState.showSnackbar("Producto guardado con éxito")
                                // Limpiar formulario
                                name = ""
                                description = ""
                                brand = ""
                                size = ""
                                selectedCategories = emptyList()
                                gender = ""
                                price = ""
                                selectedUris = emptyList()
                                uploadedUrls = emptyList()
                                uploadChecks = emptyList()
                                imageSizes.clear()
                                onNavigateBack()
                            } else {
                                error = result.exceptionOrNull()?.message
                            }
                            processing = false
                        }
                    },
                    enabled = !processing && uploadedUrls.isNotEmpty() && uploadedUrls.size == selectedUris.size
                ) {
                    Text("Finalizar carga")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
} 