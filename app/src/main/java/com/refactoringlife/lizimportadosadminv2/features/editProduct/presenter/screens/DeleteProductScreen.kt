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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.core.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeleteProductScreen() {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchField by remember { mutableStateOf("name") }
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val searchFields = listOf("name" to "Nombre", "brand" to "Marca", "categories" to "Categorías")
    var expanded by remember { mutableStateOf(false) }
    
    // Agregar ProductRepository
    val productRepository = remember { ProductRepository() }

    fun loadProducts(query: String, field: String) {
        if (query.isBlank()) {
            products = emptyList()
            loading = false
            return
        }
        loading = true
        coroutineScope.launch {
            try {
                val allProducts = productRepository.searchProductsByName(query)
                // Filtro contains insensible a mayúsculas
                products = allProducts.filter {
                    val value = when (field) {
                        "brand" -> it.brand ?: ""
                        "categories" -> it.categories?.joinToString(", ") ?: ""
                        else -> it.name ?: ""
                    }.lowercase()
                    value.contains(query.lowercase())
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

    fun deleteProduct(productId: String) {
        coroutineScope.launch {
            try {
                val result = productRepository.updateProduct(productId, mapOf("is_available" to false))
                if (result.isSuccess) {
                    products = products.filterNot { it.id == productId }
                } else {
                    error = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                error = e.message
            }
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
                            if (product.categories?.isNotEmpty() == true) {
                                Text(
                                    text = product.categories.joinToString(", "),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Button(
                            onClick = { deleteProduct(product.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF5350)
                            )
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
} 