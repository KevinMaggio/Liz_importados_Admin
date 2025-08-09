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
    val coroutineScope = rememberCoroutineScope()
    val db = remember { Firebase.firestore }

    LaunchedEffect(Unit) {
        try {
            // 1. Obtener el carrito
            val cartDoc = db.collection("carts")
                .whereEqualTo("email", email)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (cartDoc != null) {
                val productIds = (cartDoc.get("productIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val comboIds = (cartDoc.get("comboIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                // 2. Obtener productos
                val productsList = mutableListOf<ProductResponse>()
                
                // Obtener productos individuales
                for (productId in productIds) {
                    val productDoc = db.collection("products").document(productId).get().await()
                    productDoc.toObject(ProductResponse::class.java)?.let { 
                        productsList.add(it)
                    }
                }

                // Obtener productos de combos
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
                                // Agregar productos del combo con el precio del combo dividido entre 2
                                productsList.add(prod1.copy(price = newPrice / 2))
                                productsList.add(prod2.copy(price = newPrice / 2))
                            }
                        }
                    }
                }

                products = productsList

                // Calcular totales
                totalNormal = products.sumOf { it.price ?: 0 }
                totalConDescuentos = products.sumOf { prod ->
                    if (prod.isOffer == true) prod.offerPrice else prod.price ?: 0
                }

                // Marcar carrito como procesado
                cartDoc.reference.update("status", "PROCESSED")
            }
        } catch (e: Exception) {
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
        // Título y botón de volver
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detalle del Carrito",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateBack) {
                Text("Volver")
            }
        }

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
                modifier = Modifier.fillMaxWidth(),
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
                            // Imagen del producto
                            product.images?.firstOrNull()?.let { imageUrl ->
                                LipsyCardImage(
                                    url = imageUrl,
                                    modifier = Modifier.size(80.dp)
                                )
                            }

                            // Información del producto
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
        }
    }
}