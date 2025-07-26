package com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens

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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun VenderProductoScreen() {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductLight>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchField by remember { mutableStateOf("name") }
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val searchFields = listOf("name" to "Nombre", "brand" to "Marca", "category" to "Categoría")
    var expanded by remember { mutableStateOf(false) }

    fun loadProducts(query: String, field: String) {
        if (query.isBlank()) {
            products = emptyList()
            loading = false
            return
        }
        loading = true
        coroutineScope.launch {
            try {
                val db = Firebase.firestore
                var ref: Query = db.collection("products")
                val q = query.lowercase()
                ref = ref
                    .orderBy(field)
                    .startAt(q)
                    .endAt(q + "\uf8ff")
                val snapshot = ref.get().await()
                val allProducts = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name")
                    val images = doc.get("images") as? List<*>
                    val image = images?.firstOrNull() as? String
                    val brand = doc.getString("brand")
                    val category = doc.getString("category")
                    val isAvailable = doc.getBoolean("is_available") ?: true
                    ProductLight(id, name, image, brand, category).takeIf { isAvailable }
                }.filterNotNull()
                // Filtro contains insensible a mayúsculas
                products = allProducts.filter {
                    val value = when (field) {
                        "brand" -> it.brand ?: ""
                        "category" -> it.category ?: ""
                        else -> it.name ?: ""
                    }.lowercase()
                    value.contains(q)
                }
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    fun onSearchChanged(query: String, field: String) {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(500)
            loadProducts(query, field)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchChanged(it, searchField)
                },
                label = { Text("Buscar") },
                modifier = Modifier.weight(1f)
            )
            Box {
                Button(onClick = { expanded = true }) {
                    Text("Buscar por: " + (searchFields.find { it.first == searchField }?.second ?: ""))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    searchFields.forEach { (field, label) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchField == field) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Blue)
                                        Spacer(Modifier.width(4.dp))
                                        Text(label, color = Color.Blue)
                                    } else {
                                        Text(label)
                                    }
                                }
                            },
                            onClick = {
                                searchField = field
                                expanded = false
                                onSearchChanged(searchQuery, field)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Text("Error: $error", color = Color.Red)
            searchQuery.isBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Ingrese un término de búsqueda") }
            products.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No se encontraron productos con esa búsqueda.") }
            else -> LazyColumn {
                items(products) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                        Text(text = product.name ?: "Sin nombre", color = Color.Black, modifier = Modifier.weight(1f))
                        Button(onClick = {
                            coroutineScope.launch {
                                try {
                                    val db = Firebase.firestore
                                    db.collection("products").document(product.id).update("is_available", false).await()
                                    products = products.filterNot { it.id == product.id }
                                } catch (e: Exception) {
                                    error = e.message
                                }
                            }
                        }) {
                            Text("Vendido")
                        }
                    }
                }
            }
        }
    }
} 