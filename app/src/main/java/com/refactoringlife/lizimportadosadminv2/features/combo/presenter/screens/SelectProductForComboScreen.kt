package com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens

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
import coil.compose.AsyncImage
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel.ComboProduct
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel.ComboViewModel

// Modelo ligero para la lista
data class ProductLightForCombo(
    val id: String,
    val name: String?,
    val image: String?,
    val brand: String? = null,
    val categories: List<String>? = null,
    val price: Int = 0
)

// Modelo específico para productos en combos
data class ComboProduct(
    val id: String,
    val name: String?, // Cambiar de title a name
    val price: Int?,
    val images: List<String>?
)

@Composable
fun SelectProductForComboScreen(
    productNumber: Int, // 1 o 2 para indicar qué producto seleccionar
    viewModel: ComboViewModel,
    onNavigateBack: () -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductLightForCombo>>(emptyList()) }
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
                    val price = doc.getLong("price")?.toInt() ?: 0
                    ProductLightForCombo(id, name, image, brand, categoriesList, price)
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
                
                Log.d("SelectProductForComboScreen", "🔍 Búsqueda: '$q' en campo '$field'")
                Log.d("SelectProductForComboScreen", "📊 Productos encontrados: ${products.size}")
                products.forEach { product ->
                    Log.d("SelectProductForComboScreen", "✅ Producto: ${product.name} (ID: ${product.id})")
                }
                
                error = null
            } catch (e: Exception) {
                Log.e("SelectProductForComboScreen", "❌ Error cargando productos: ${e.message}")
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
        //

        Text(
            text = "Seleccionar Producto $productNumber",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
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
                            .clickable { 
                                // Convertir ProductLightForCombo a ComboProduct
                                val comboProduct = ComboProduct(
                                    id = product.id,
                                    name = product.name,
                                    price = product.price,
                                    images = listOf(product.image ?: "")
                                )
                                
                                // Guardar en el ViewModel según el número de producto
                                when (productNumber) {
                                    1 -> viewModel.setProduct1(comboProduct)
                                    2 -> viewModel.setProduct2(comboProduct)
                                }
                                
                                onNavigateBack()
                            }
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.name ?: "Sin nombre", color = Color.Black)
                            Text(text = "Precio: $${product.price}", color = Color.Gray)
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