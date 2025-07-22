package com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens

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
import com.refactoringlife.lizimportadosadmin.core.dto.request.ProductRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background

@Composable
fun EditProductDetailScreen(productId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<ProductRequest?>(null) }
    var original by remember { mutableStateOf<ProductRequest?>(null) }
    val coroutineScope = rememberCoroutineScope()
    // Estados editables
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var comboId by remember { mutableStateOf("") }
    var comboPrice by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(false) }
    var isOffer by remember { mutableStateOf(false) }
    var offerPrice by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("") }
    var circleOptionFilter by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
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

    LaunchedEffect(productId) {
        loading = true
        try {
            val db = Firebase.firestore
            val doc = db.collection("products").document(productId).get().await()
            val prod = doc.toObject(ProductRequest::class.java)
            if (prod != null) {
                product = prod
                original = prod.copy()
                name = prod.name ?: ""
                description = prod.description ?: ""
                brand = prod.brand ?: ""
                category = prod.category ?: ""
                comboId = prod.comboId?.joinToString(",") ?: ""
                comboPrice = prod.comboPrice?.toString() ?: ""
                gender = prod.gender ?: ""
                isAvailable = prod.isAvailable == true
                isOffer = prod.isOffer == true
                offerPrice = prod.offerPrice.toString()
                price = prod.price?.toString() ?: ""
                season = prod.season ?: ""
                circleOptionFilter = prod.circleOptionFilter ?: ""
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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: $error") }
        return
    }
    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Producto no encontrado") }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Ahora es scrolleable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Editar Producto", modifier = Modifier.padding(bottom = 16.dp))
        if (images.isNotEmpty()) {
            AsyncImage(model = images.first(), contentDescription = "Imagen producto", modifier = Modifier.size(100.dp))
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.name ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.description ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.brand ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.category ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = comboId, onValueChange = { comboId = it }, label = { Text("Combo IDs (separados por coma)") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.comboId?.joinToString(",") ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = comboPrice, onValueChange = { comboPrice = it }, label = { Text("Combo Precio") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.comboPrice ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.gender ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = offerPrice, onValueChange = { offerPrice = it }, label = { Text("Precio Oferta") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.offerPrice ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.price ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = season, onValueChange = { season = it }, label = { Text("Temporada") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.season ?: "-"}", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(value = circleOptionFilter, onValueChange = { circleOptionFilter = it }, label = { Text("Filtro Círculo") }, modifier = Modifier.fillMaxWidth())
        Text("Anterior: ${original?.circleOptionFilter ?: "-"}", fontSize = 12.sp, color = Color.Gray)
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    val cambios = mutableMapOf<String, Any>()
                    if (name != original?.name) cambios["name"] = name
                    if (description != original?.description) cambios["description"] = description
                    if (brand != original?.brand) cambios["brand"] = brand
                    if (category != original?.category) cambios["category"] = category
                    if (comboId != (original?.comboId?.joinToString(",") ?: "")) cambios["combo_id"] = comboId.split(",").map { it.trim() }
                    if (comboPrice != (original?.comboPrice?.toString() ?: "")) cambios["combo_price"] = comboPrice.toIntOrNull() ?: 0
                    if (gender != original?.gender) cambios["gender"] = gender
                    if (isAvailable != (original?.isAvailable == true)) cambios["is_available"] = isAvailable
                    if (isOffer != (original?.isOffer == true)) cambios["is_offer"] = isOffer
                    if (offerPrice != (original?.offerPrice?.toString() ?: "")) cambios["offer_price"] = offerPrice.toIntOrNull() ?: 0
                    if (price != (original?.price?.toString() ?: "")) cambios["price"] = price.toIntOrNull() ?: 0
                    if (season != original?.season) cambios["season"] = season
                    if (circleOptionFilter != original?.circleOptionFilter) cambios["circle_option_filter"] = circleOptionFilter
                    if (cambios.isNotEmpty()) {
                        try {
                            val db = Firebase.firestore
                            db.collection("products").document(productId).update(cambios).await()
                            dialogMessage = "¡Producto actualizado exitosamente!"
                            dialogIsError = false
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
            enabled = !isSaving
        ) {
            Text("Guardar Cambios")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack) { Text("Volver") }
    }
} 