package com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.refactoringlife.lizimportadosadmin.core.dto.request.ProductRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun DeleteProductScreen() {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductRequest>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    fun loadProducts() {
        loading = true
        coroutineScope.launch {
            try {
                val db = Firebase.firestore
                val snapshot = db.collection("products").get().await()
                products = snapshot.documents.mapNotNull { it.toObject(ProductRequest::class.java) }
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadProducts() }

    fun deleteProduct(productId: String) {
        coroutineScope.launch {
            try {
                val db = Firebase.firestore
                db.collection("products").document(productId).delete().await()
                products = products.filterNot { it.id == productId }
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Error: $error", color = Color.Red)
            else -> LazyColumn {
                items(products) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!product.images.isNullOrEmpty()) {
                            AsyncImage(
                                model = product.images.first(),
                                contentDescription = "Imagen producto",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.name ?: "Sin nombre", color = Color.Black)
                            Text(text = product.description ?: "", color = Color.Gray, fontSize = 12.sp)
                        }
                        Button(onClick = { deleteProduct(product.id) }) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
} 