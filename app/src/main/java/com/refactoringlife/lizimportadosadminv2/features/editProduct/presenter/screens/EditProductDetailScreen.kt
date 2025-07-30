package com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.core.utils.ProductConstants
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyDropdown
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyMultiSelect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import com.refactoringlife.lizimportadosadminv2.core.repository.ProductRepository

@Composable
fun EditProductDetailScreen(productId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<ProductResponse?>(null) }
    var original by remember { mutableStateOf<ProductResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Estados editables
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var comboIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var gender by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(false) }
    var isOffer by remember { mutableStateOf(false) }
    var offerPrice by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Estados de UI
    var success by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var dialogIsError by remember { mutableStateOf(false) }

    // Modal de confirmación/error
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!dialogIsError) {
                    onBack()
                }
                showDialog = false
            },
            title = { Text(if (dialogIsError) "Error" else "¡Éxito!") },
            text = { Text(dialogMessage ?: "") },
            confirmButton = {
                Button(
                    onClick = {
                        if (!dialogIsError) {
                            onBack()
                        }
                        showDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = if (dialogIsError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        )
    }

    // Loading overlay
    if (isSaving) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Guardando cambios...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }

    // Agregar ProductRepository
    val productRepository = remember { ProductRepository() }

    LaunchedEffect(productId) {
        loading = true
        try {
            val prod = productRepository.getProduct(productId)
            if (prod != null) {
                product = prod
                original = prod.copy()
                name = prod.name ?: ""
                description = prod.description ?: ""
                brand = prod.brand ?: ""
                size = prod.size ?: ""
                selectedCategories = prod.categories ?: emptyList()
                comboIds = prod.comboIds ?: emptyList()
                gender = prod.gender ?: ""
                
                // Debug: Log de valores booleanos
                android.util.Log.d("EditProductDetail", "Raw isAvailable: ${prod.isAvailable}")
                android.util.Log.d("EditProductDetail", "Raw isOffer: ${prod.isOffer}")
                
                isAvailable = prod.isAvailable == true
                isOffer = prod.isOffer == true
                
                // Debug: Log de valores procesados
                android.util.Log.d("EditProductDetail", "Processed isAvailable: $isAvailable")
                android.util.Log.d("EditProductDetail", "Processed isOffer: $isOffer")
                
                offerPrice = prod.offerPrice.toString()
                price = prod.price?.toString() ?: ""
                images = prod.images ?: emptyList()
            }
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            CircularProgressIndicator() 
        }
        return
    }
    
    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Text("Error: $error") 
        }
        return
    }
    
    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Text("Producto no encontrado") 
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Editar Producto", 
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Imagen del producto
        if (images.isNotEmpty()) {
            AsyncImage(
                model = images.first(), 
                contentDescription = "Imagen producto", 
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Nombre
        OutlinedTextField(
            value = name, 
            onValueChange = { name = it }, 
            label = { Text("Nombre") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.name ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Descripción
        OutlinedTextField(
            value = description, 
            onValueChange = { description = it }, 
            label = { Text("Descripción") }, 
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Text("Anterior: ${original?.description ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Marca
        OutlinedTextField(
            value = brand, 
            onValueChange = { brand = it }, 
            label = { Text("Marca") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.brand ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Talla
        OutlinedTextField(
            value = size, 
            onValueChange = { size = it }, 
            label = { Text("Talla") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.size ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Categorías múltiples
        LipsyMultiSelect(
            label = "Categorías",
            options = ProductConstants.CATEGORIES,
            selectedOptions = selectedCategories,
            onSelectionChanged = { selectedCategories = it },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.categories?.joinToString(", ") ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Género (Dropdown)
        LipsyDropdown(
            label = "Género",
            options = ProductConstants.GENDERS,
            selectedOption = gender,
            onOptionSelected = { gender = it },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.gender ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Precio
        OutlinedTextField(
            value = price, 
            onValueChange = { price = it }, 
            label = { Text("Precio") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.price ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Precio de oferta
        OutlinedTextField(
            value = offerPrice, 
            onValueChange = { offerPrice = it }, 
            label = { Text("Precio de Oferta") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Text("Anterior: ${original?.offerPrice ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Combos asociados (solo lectura)
        if (comboIds.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Combos asociados:",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    comboIds.forEach { comboId ->
                        Text("• Combo #$comboId")
                    }
                    Text(
                        "Nota: Los combos se gestionan desde la sección 'Gestionar Combos'",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Checkboxes
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isAvailable, onCheckedChange = { isAvailable = it })
            Text("Disponible")
        }
        Text("Anterior: ${original?.isAvailable ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isOffer, onCheckedChange = { isOffer = it })
            Text("En Oferta")
        }
        Text("Anterior: ${original?.isOffer ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botones
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    val cambios = mutableMapOf<String, Any>()
                    
                    if (name != original?.name) cambios["name"] = name
                    if (description != original?.description) cambios["description"] = description
                    if (brand != original?.brand) cambios["brand"] = brand
                    if (size != original?.size) cambios["size"] = size
                    if (selectedCategories != original?.categories) cambios["categories"] = selectedCategories
                    if (gender != original?.gender) cambios["gender"] = gender
                    if (isAvailable != (original?.isAvailable == true)) cambios["is_available"] = isAvailable
                    if (isOffer != (original?.isOffer == true)) cambios["is_offer"] = isOffer
                    if (offerPrice != (original?.offerPrice?.toString() ?: "")) cambios["offer_price"] = offerPrice.toIntOrNull() ?: 0
                    if (price != (original?.price?.toString() ?: "")) cambios["price"] = price.toIntOrNull() ?: 0
                    
                    if (cambios.isNotEmpty()) {
                        try {
                            val result = productRepository.updateProduct(productId, cambios)
                            if (result.isSuccess) {
                                dialogMessage = "¡Producto actualizado exitosamente!"
                                dialogIsError = false
                            } else {
                                dialogMessage = "Error al actualizar: ${result.exceptionOrNull()?.message}"
                                dialogIsError = true
                            }
                        } catch (e: Exception) {
                            dialogMessage = "Error al actualizar: ${e.message}"
                            dialogIsError = true
                        }
                    } else {
                        dialogMessage = "No hay cambios para guardar"
                        dialogIsError = false
                    }
                    isSaving = false
                    showDialog = true
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Cambios")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) { 
            Text("Volver") 
        }
    }
} 