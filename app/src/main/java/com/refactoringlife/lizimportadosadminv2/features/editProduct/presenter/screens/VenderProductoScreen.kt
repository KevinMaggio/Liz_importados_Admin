package com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.util.Date

data class ProductForSale(
    val id: String,
    val name: String?,
    val image: String?,
    val brand: String?,
    val categories: List<String>?,
    val price: Int,
    val isCombo: Boolean = false,
    val comboId: Int? = null
)

@Composable
fun VenderProductoScreen(
    onNavigateBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductForSale>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductForSale?>(null) }
    var processingVenta by remember { mutableStateOf(false) }

    // Dialog de confirmación de venta
    if (showDialog && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Venta") },
            text = { 
                Column {
                    Text("¿Confirmar la venta de:")
                    Text(
                        text = selectedProduct?.name ?: "",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text("Precio: $${selectedProduct?.price}")
                    if (selectedProduct?.isCombo == true) {
                        Text(
                            text = "(Combo #${selectedProduct?.comboId})",
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            processingVenta = true
                            try {
                                val db = Firebase.firestore
                                val batch = db.batch()
                                
                                // 1. Registrar la venta
                                val ventaRef = db.collection("ventas").document()
                                val ventaData = mapOf(
                                    "fecha" to Date(),
                                    "producto_id" to selectedProduct!!.id,
                                    "precio" to selectedProduct!!.price,
                                    "es_combo" to selectedProduct!!.isCombo,
                                    "combo_id" to selectedProduct!!.comboId
                                )
                                batch.set(ventaRef, ventaData)
                                
                                // 2. Actualizar estadísticas del producto y marcar como no disponible
                                val productRef = db.collection("products").document(selectedProduct!!.id)
                                val productDoc = productRef.get().await()
                                val ventasActuales = productDoc.getLong("vendidos")?.toInt() ?: 0
                                batch.update(productRef, mapOf(
                                    "vendidos" to (ventasActuales + 1),
                                    "is_available" to false
                                ))
                                
                                // 3. Si es combo, actualizar estadísticas del combo
                                if (selectedProduct!!.isCombo && selectedProduct!!.comboId != null) {
                                    val comboRef = db.collection("combos").document(selectedProduct!!.comboId.toString())
                                    batch.update(comboRef, "is_available", false)
                                }
                                
                                batch.commit().await()
                                
                                // Actualizar UI
                                showDialog = false
                                selectedProduct = null
                                // Remover el producto vendido de la lista
                                products = products.filterNot { it.id == selectedProduct!!.id }
                                
                                // Mostrar mensaje de éxito
                                error = null
                                
                            } catch (e: Exception) {
                                Log.e("VenderProductoScreen", "Error registrando venta: ${e.message}")
                                error = "Error al registrar la venta: ${e.message}"
                            } finally {
                                processingVenta = false
                            }
                        }
                    },
                    enabled = !processingVenta
                ) {
                    Text(if (processingVenta) "Procesando..." else "Confirmar Venta")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    enabled = !processingVenta
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(searchQuery) {
        loading = true
        try {
            val db = Firebase.firestore
            val productsList = mutableListOf<ProductForSale>()
            
            // 1. Obtener productos normales
            val productsQuery = if (searchQuery.isBlank()) {
                db.collection("products").whereEqualTo("is_available", true)
            } else {
                db.collection("products")
                    .whereEqualTo("is_available", true)
                    .whereGreaterThanOrEqualTo("name", searchQuery)
                    .whereLessThanOrEqualTo("name", searchQuery + '\uf8ff')
            }
            
            val productsSnapshot = productsQuery.get().await()
            for (doc in productsSnapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name")
                val images = doc.get("images") as? List<*>
                val image = images?.firstOrNull() as? String
                val brand = doc.getString("brand")
                val categories = doc.get("categories") as? List<*>
                val categoriesList = categories?.mapNotNull { it as? String }
                val price = doc.getLong("price")?.toInt() ?: 0
                val comboIds = doc.get("combo_ids") as? List<*>
                
                // Si el producto está en un combo, obtener el combo
                if (!comboIds.isNullOrEmpty()) {
                    for (comboId in comboIds) {
                        val comboDoc = db.collection("combos")
                            .document(comboId.toString())
                            .get()
                            .await()
                        
                        if (comboDoc.exists() && comboDoc.getBoolean("is_available") == true) {
                            val comboIdInt = comboDoc.getLong("combo_id")?.toInt()
                            val comboPrice = comboDoc.getLong("new_price")?.toInt() ?: 0
                            
                            productsList.add(
                                ProductForSale(
                                    id = id,
                                    name = name,
                                    image = image,
                                    brand = brand,
                                    categories = categoriesList,
                                    price = comboPrice,
                                    isCombo = true,
                                    comboId = comboIdInt
                                )
                            )
                        }
                    }
                }
                
                // Agregar el producto normal
                productsList.add(
                    ProductForSale(
                        id = id,
                        name = name,
                        image = image,
                        brand = brand,
                        categories = categoriesList,
                        price = price
                    )
                )
            }
            
            products = productsList.sortedWith(
                compareBy<ProductForSale> { !it.isCombo }
                    .thenBy { it.name }
            )
            error = null
        } catch (e: Exception) {
            Log.e("VenderProductoScreen", "Error cargando productos: ${e.message}")
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Vender Producto",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            products.isEmpty() -> {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "No hay productos disponibles para vender"
                    } else {
                        "No se encontraron productos con ese nombre"
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedProduct = product
                                    showDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                product.image?.let { image ->
                                    AsyncImage(
                                        model = image,
                                        contentDescription = "Producto",
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name ?: "Sin nombre",
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (product.brand != null) {
                                        Text(
                                            text = product.brand,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (product.categories?.isNotEmpty() == true) {
                                        Text(
                                            text = product.categories.joinToString(", "),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (product.isCombo) {
                                        Text(
                                            text = "Combo #${product.comboId}",
                                            color = Color(0xFF1976D2),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "$${product.price}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.isCombo) Color(0xFF388E3C) else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
} 