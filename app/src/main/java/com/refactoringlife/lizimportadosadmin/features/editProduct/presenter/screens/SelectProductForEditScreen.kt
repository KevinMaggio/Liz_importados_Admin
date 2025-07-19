package com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Modelo ligero para la lista
 data class ProductLight(
    val id: String,
    val name: String?,
    val image: String?
)

@Composable
fun SelectProductForEditScreen(onProductSelected: (String) -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductLight>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        coroutineScope.launch {
            try {
                val db = Firebase.firestore
                val snapshot = db.collection("products").get().await()
                products = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name")
                    val images = doc.get("images") as? List<*>
                    val image = images?.firstOrNull() as? String
                    ProductLight(id, name, image)
                }
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Error: $error")
            else -> LazyColumn {
                items(products) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductSelected(product.id) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (product.image != null) {
                            AsyncImage(
                                model = product.image,
                                contentDescription = "Imagen producto",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(text = product.name ?: "Sin nombre", color = Color.Black)
                    }
                }
            }
        }
    }
} 