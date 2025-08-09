package com.refactoringlife.lizimportadosadminv2.features.carts.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyCardImage
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart

@Composable
fun CartDetailScreen(
    email: String,
    onNavigateBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var totalNormal by remember { mutableStateOf(0) }
    var totalConDescuentos by remember { mutableStateOf(0) }
    var cartStatus by remember { mutableStateOf("") }
    var cartDocId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val db = remember { Firebase.firestore }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            val cartDoc = db.collection("carts")
                .whereEqualTo("email", email)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (cartDoc != null) {
                cartDocId = cartDoc.id
                cartStatus = cartDoc.getString("status") ?: "AVAILABLE"
                val productIds = (cartDoc.get("productIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val comboIds = (cartDoc.get("comboIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                val productsList = mutableListOf<ProductResponse>()
                
                for (productId in productIds) {
                    val productDoc = db.collection("products").document(productId).get().await()
                    productDoc.toObject(ProductResponse::class.java)?.let { 
                        productsList.add(it)
                    }
                }

                for (comboId in comboIds) {
                    val comboDoc = db.collection("combos").document(comboId).get().await()
                    val product1Id = comboDoc.getString("product1Id")
                    val product2Id = comboDoc.getString("product2Id")
                    val newPrice = comboDoc.getLong("newPrice")?.toInt() ?: 0

                    if (product1Id != null && product2Id != null) {
                        val product1Doc = db.collection("products").document(product1Id).get().await()
                        val product2Doc = db.collection("products").document(product2Id).get().await()
                        
                        product1Doc.toObject(ProductResponse::class.java)?.let { prod1 ->
                            product2Doc.toObject(ProductResponse::class.java)?.let { prod2 ->
                                productsList.add(prod1.copy(price = newPrice / 2))
                                productsList.add(prod2.copy(price = newPrice / 2))
                            }
                        }
                    }
                }

                products = productsList
                totalNormal = products.sumOf { it.price ?: 0 }
                totalConDescuentos = products.sumOf { prod ->
                    if (prod.isOffer == true) prod.offerPrice else prod.price ?: 0
                }

                if (cartStatus == "AVAILABLE") {
                    cartDoc.reference.update("status", "PROCESSED")
                    cartStatus = "PROCESSED"
                }
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Detalle del Carrito",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Estado: ${
                    when (cartStatus) {
                        "AVAILABLE" -> "ACTIVO"
                        "PROCESSED" -> "EN PROCESO"
                        "SOLD" -> "VENDIDO"
                        else -> "DESCONOCIDO"
                    }
                }",
                color = when (cartStatus) {
                    "AVAILABLE" -> Color.Green
                    "PROCESSED" -> Color(0xFFFFA000)
                    "SOLD" -> Color(0xFFF44336)
                    else -> Color.Gray
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Email: $email",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Totales
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total Normal: $$totalNormal",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Total con Descuentos: $$totalConDescuentos",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (totalConDescuentos < totalNormal) Color.Green else Color.Unspecified
                        )
                        if (totalConDescuentos < totalNormal) {
                            Text(
                                text = "Ahorro: $${totalNormal - totalConDescuentos}",
                                color = Color.Green
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de productos
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                product.images?.firstOrNull()?.let { imageUrl ->
                                    LipsyCardImage(
                                        url = imageUrl,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = product.name ?: "Sin nombre",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (product.isOffer == true) {
                                        Text(
                                            text = "Precio Original: $${product.price}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "Precio Oferta: $${product.offerPrice}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Green
                                        )
                                    } else {
                                        Text(
                                            text = "Precio: $${product.price}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones según el estado
                if (cartStatus != "SOLD") {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val batch = db.batch()
                                    products.forEach { product ->
                                        val productRef = db.collection("products").document(product.id)
                                        batch.update(productRef, mapOf(
                                            "is_available" to false,
                                            "vendidos" to FieldValue.increment(1)
                                        ))
                                    }
                                    val cartRef = db.collection("carts").document(cartDocId)
                                    batch.update(cartRef, "status", "SOLD")
                                    batch.commit().await()
                                    cartStatus = "SOLD"
                                    snackbarHostState.showSnackbar("Carrito vendido exitosamente")
                                } catch (e: Exception) {
                                    error = "Error al vender carrito: ${e.message}"
                                    snackbarHostState.showSnackbar("Error al vender carrito")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text(if (cartStatus == "AVAILABLE") "VENDER CARRITO" else "CONFIRMAR VENTA")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (cartStatus == "PROCESSED") {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    db.collection("carts").document(cartDocId)
                                        .update("status", "AVAILABLE")
                                        .await()
                                    cartStatus = "AVAILABLE"
                                    snackbarHostState.showSnackbar("Carrito reactivado exitosamente")
                                } catch (e: Exception) {
                                    error = "Error al reactivar carrito: ${e.message}"
                                    snackbarHostState.showSnackbar("Error al reactivar carrito")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("REACTIVAR CARRITO")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (cartStatus == "SOLD") {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val batch = db.batch()
                                    val cartRef = db.collection("carts").document(cartDocId)
                                    batch.update(cartRef, mapOf(
                                        "status" to "AVAILABLE",
                                        "productIds" to emptyList<String>(),
                                        "comboIds" to emptyList<String>()
                                    ))
                                    batch.commit().await()
                                    cartStatus = "AVAILABLE"
                                    products = emptyList()
                                    totalNormal = 0
                                    totalConDescuentos = 0
                                    snackbarHostState.showSnackbar("Carrito limpiado exitosamente")
                                } catch (e: Exception) {
                                    error = "Error al limpiar carrito: ${e.message}"
                                    snackbarHostState.showSnackbar("Error al limpiar carrito")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("LIMPIAR Y REACTIVAR CARRITO")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Botón Volver (siempre visible)
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("VOLVER")
                }
            }
        }
    }
}