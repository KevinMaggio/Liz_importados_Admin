package com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel.ComboProduct
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel.ComboViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun CreateComboScreen(
    viewModel: ComboViewModel,
    onNavigateToSelectProduct: (Int) -> Unit, // 1 o 2 para indicar qué producto seleccionar
    onNavigateBack: () -> Unit
) {
    var comboId by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }
    
    // Observar productos del ViewModel
    val product1 by viewModel.product1.collectAsState()
    val product2 by viewModel.product2.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    // Generar ID automático del combo
    LaunchedEffect(Unit) {
        comboId = "COMBO_${System.currentTimeMillis()}"
    }
    
    // Calcular precio total de forma segura
    val oldPrice = (product1?.price ?: 0) + (product2?.price ?: 0)
    
    // Validar que los productos no sean el mismo
    val isSameProduct = product1?.id == product2?.id && product1 != null && product2 != null
    
    // Validar precio del combo
    val isValidPrice = newPrice.isNotBlank() && newPrice.toIntOrNull() != null && 
                      newPrice.toIntOrNull()!! > 0
    
    // Validar que el combo sea válido
    val isValidCombo = comboId.isNotBlank() && product1 != null && product2 != null && 
                      !isSameProduct && isValidPrice
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Crear Combo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // ID del Combo (solo lectura)
        OutlinedTextField(
            value = comboId,
            onValueChange = { }, // No permitir edición
            label = { Text("ID del Combo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = false, // Deshabilitar edición
            readOnly = true
        )
        
        // Producto 1
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (product1 != null) "Producto 1: ${product1?.title ?: "Sin nombre"}" else "Producto 1",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (product1 != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = product1?.images?.firstOrNull(),
                            contentDescription = "Imagen producto 1",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product1?.title ?: "Sin nombre", fontWeight = FontWeight.Bold)
                            Text(text = "Precio: $${product1?.price ?: 0}")
                        }
                        IconButton(onClick = { viewModel.clearProduct1() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                } else {
                    Button(
                        onClick = { onNavigateToSelectProduct(1) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar Producto 1")
                    }
                }
            }
        }
        
        // Producto 2
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (product2 != null) "Producto 2: ${product2?.title ?: "Sin nombre"}" else "Producto 2",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (product2 != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = product2?.images?.firstOrNull(),
                            contentDescription = "Imagen producto 2",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product2?.title ?: "Sin nombre", fontWeight = FontWeight.Bold)
                            Text(text = "Precio: $${product2?.price ?: 0}")
                        }
                        IconButton(onClick = { viewModel.clearProduct2() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                } else {
                    Button(
                        onClick = { onNavigateToSelectProduct(2) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar Producto 2")
                    }
                }
            }
        }
        
        // Precios
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Precios",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text("Precio Original: $${oldPrice}")
                Text("Precio del Combo:", modifier = Modifier.padding(top = 8.dp))
                
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            newPrice = it
                        }
                    },
                    label = { Text("Precio del Combo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    isError = newPrice.isNotBlank() && !isValidPrice
                )
                
                if (newPrice.isNotBlank() && !isValidPrice) {
                    Text(
                        text = "El precio debe ser mayor a 0",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (isSameProduct) {
                    Text(
                        text = "No puedes seleccionar el mismo producto",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botón crear combo
        Button(
            onClick = {
                if (!isValidCombo) {
                    error = when {
                        comboId.isBlank() -> "Ingresa un ID para el combo"
                        product1 == null -> "Selecciona el primer producto"
                        product2 == null -> "Selecciona el segundo producto"
                        isSameProduct -> "No puedes seleccionar el mismo producto"
                        !isValidPrice -> "Ingresa un precio válido"
                        else -> "Completa todos los campos"
                    }
                    return@Button
                }
                
                loading = true
                error = null
                coroutineScope.launch {
                    try {
                        val combo = ComboRequest(
                            id = UUID.randomUUID().toString(),
                            comboId = comboId,
                            oldPrice = oldPrice,
                            newPrice = newPrice.toInt(),
                            product1Id = product1?.id ?: "",
                            product2Id = product2?.id ?: ""
                        )
                        
                        // Guardar combo
                        val db = Firebase.firestore
                        db.collection("combos").document(combo.id).set(combo).await()
                        
                        // Actualizar productos con comboId
                        product1?.id?.let { product1Id ->
                            val product1Ref = db.collection("products").document(product1Id)
                            val product1Doc = product1Ref.get().await()
                            val currentComboIds = product1Doc.get("combo_ids") as? List<String> ?: emptyList()
                            product1Ref.update("combo_ids", currentComboIds + combo.comboId).await()
                        }
                        
                        product2?.id?.let { product2Id ->
                            val product2Ref = db.collection("products").document(product2Id)
                            val product2Doc = product2Ref.get().await()
                            val currentComboIds2 = product2Doc.get("combo_ids") as? List<String> ?: emptyList()
                            product2Ref.update("combo_ids", currentComboIds2 + combo.comboId).await()
                        }
                        
                        success = "Combo creado exitosamente"
                        onNavigateBack()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && isValidCombo,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Crear Combo")
            }
        }
        
        if (error != null) {
            Text(
                text = error!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (success != null) {
            Text(
                text = success!!,
                color = Color.Green,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
} 