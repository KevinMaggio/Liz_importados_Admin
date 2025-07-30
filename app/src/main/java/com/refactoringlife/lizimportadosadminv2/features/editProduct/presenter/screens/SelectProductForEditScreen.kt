package com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

// Modelo ligero para la lista
data class ProductLight(
    val id: String,
    val name: String?,
    val image: String?,
    val brand: String? = null,
    val categories: List<String>? = null
)

@Composable
fun SelectProductForEditScreen(onProductSelected: (String) -> Unit) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductLight>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchField by remember { mutableStateOf("name") }
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val searchFields = listOf("name" to "Nombre", "brand" to "Marca", "categories" to "Categorías")
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
                val q = query.lowercase()
                
                // Búsqueda optimizada pero manteniendo la estructura original
                val snapshot = db.collection("products").get().await()
                val allProducts = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name")
                    val images = doc.get("images") as? List<*>
                    val image = images?.firstOrNull() as? String
                    val brand = doc.getString("brand")
                    val categories = doc.get("categories") as? List<*>
                    val categoriesList = categories?.mapNotNull { it as? String }
                    ProductLight(id, name, image, brand, categoriesList)
                }
                
                // Filtro contains insensible a mayúsculas - más flexible
                products = allProducts.filter {
                    val value = when (field) {
                        "brand" -> it.brand ?: ""
                        "categories" -> it.categories?.joinToString(", ") ?: ""
                        else -> it.name ?: ""
                    }.lowercase()
                    value.contains(q)
                }
                
                Log.d("SelectProductForEditScreen", "🔍 Búsqueda: '$q' en campo '$field'")
                Log.d("SelectProductForEditScreen", "📊 Productos encontrados: ${products.size}")
                products.forEach { product ->
                    Log.d("SelectProductForEditScreen", "✅ Producto: ${product.name} (ID: ${product.id})")
                }
                
                error = null
            } catch (e: Exception) {
                Log.e("SelectProductForEditScreen", "❌ Error cargando productos: ${e.message}")
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
                        Column {
                            Text(text = product.name ?: "Sin nombre", color = Color.Black)
                            if (product.categories?.isNotEmpty() == true) {
                                Text(
                                    text = "Categorías: ${product.categories.joinToString(", ")}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 