package com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.refactoringlife.lizimportadosadmin.core.dto.request.ProductRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun EditProductScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (loading, setLoading) = remember { mutableStateOf(true) }
    val (products, setProducts) = remember { mutableStateOf<List<ProductRequest>>(emptyList()) }
    val (selectedProduct, setSelectedProduct) = remember { mutableStateOf<ProductRequest?>(null) }
    val (error, setError) = remember { mutableStateOf<String?>(null) }
    val (success, setSuccess) = remember { mutableStateOf<String?>(null) }

    // Formulario de edición
    val (name, setName) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }
    val (brand, setBrand) = remember { mutableStateOf("") }
    val (category, setCategory) = remember { mutableStateOf("") }
    val (comboId, setComboId) = remember { mutableStateOf("") }
    val (comboPrice, setComboPrice) = remember { mutableStateOf("") }
    val (gender, setGender) = remember { mutableStateOf("") }
    val (isAvailable, setIsAvailable) = remember { mutableStateOf(false) }
    val (isOffer, setIsOffer) = remember { mutableStateOf(false) }
    val (offerPrice, setOfferPrice) = remember { mutableStateOf("") }
    val (price, setPrice) = remember { mutableStateOf("") }
    val (season, setSeason) = remember { mutableStateOf("") }
    val (circleOptionFilter, setCircleOptionFilter) = remember { mutableStateOf("") }

    // Cargar productos solo una vez
    LaunchedEffect(Unit) {
        setLoading(true)
        val result = getAllProducts()
        if (result.isSuccess) {
            setProducts(result.getOrNull() ?: emptyList())
        } else {
            setError(result.exceptionOrNull()?.message)
        }
        setLoading(false)
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (selectedProduct == null) {
            // Lista de productos
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    val imageUrl = product.images?.firstOrNull()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                setSelectedProduct(product)
                                // Precargar los valores en el formulario
                                setName(product.name ?: "")
                                setDescription(product.description ?: "")
                                setBrand(product.brand ?: "")
                                setCategory(product.category ?: "")
                                setComboId(product.comboId?.joinToString(",") ?: "")
                                setComboPrice(product.comboPrice?.toString() ?: "")
                                setGender(product.gender ?: "")
                                setIsAvailable(product.isAvailable == true)
                                setIsOffer(product.isOffer == true)
                                setOfferPrice(product.offerPrice.toString())
                                setPrice(product.price?.toString() ?: "")
                                setSeason(product.season ?: "")
                                setCircleOptionFilter(product.circleOptionFilter ?: "")
                            }
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = imageUrl,
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
        } else {
            // Formulario de edición
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Editar Producto", modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = name, onValueChange = setName, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = setDescription, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = brand, onValueChange = setBrand, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = setCategory, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = comboId, onValueChange = setComboId, label = { Text("Combo IDs (separados por coma)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = comboPrice, onValueChange = setComboPrice, label = { Text("Combo Precio") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gender, onValueChange = setGender, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = offerPrice, onValueChange = setOfferPrice, label = { Text("Precio Oferta") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = setPrice, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = season, onValueChange = setSeason, label = { Text("Temporada") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = circleOptionFilter, onValueChange = setCircleOptionFilter, label = { Text("Filtro Círculo") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(checked = isAvailable, onCheckedChange = setIsAvailable)
                    Text("Disponible")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(checked = isOffer, onCheckedChange = setIsOffer)
                    Text("En Oferta")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val updatedProduct = selectedProduct.copy(
                                name = name.ifBlank { null },
                                description = description.ifBlank { null },
                                brand = brand.ifBlank { null },
                                category = category.ifBlank { null },
                                comboId = if (comboId.isNotBlank()) comboId.split(",").map { it.trim() } else null,
                                comboPrice = comboPrice.toIntOrNull(),
                                gender = gender.ifBlank { null },
                                isAvailable = isAvailable,
                                isOffer = isOffer,
                                offerPrice = offerPrice.toIntOrNull() ?: 0,
                                price = price.toIntOrNull(),
                                season = season.ifBlank { null },
                                circleOptionFilter = circleOptionFilter.ifBlank { null }
                            )
                            val result = updateProductInFirestore(updatedProduct)
                            if (result.isSuccess) {
                                setSuccess("Producto actualizado")
                                setSelectedProduct(null)
                                // Recargar productos
                                setLoading(true)
                                val reload = getAllProducts()
                                setProducts(reload.getOrNull() ?: emptyList())
                                setLoading(false)
                            } else {
                                setError(result.exceptionOrNull()?.message)
                            }
                        }
                    },
                    enabled = !loading
                ) {
                    Text("Guardar Cambios")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { setSelectedProduct(null) }) {
                    Text("Cancelar")
                }
                if (error != null) {
                    Text(text = "Error: $error", modifier = Modifier.padding(top = 16.dp))
                }
                if (success != null) {
                    Text(text = success, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}

suspend fun getAllProducts(): Result<List<ProductRequest>> = withContext(Dispatchers.IO) {
    try {
        val db = Firebase.firestore
        val snapshot = db.collection("products").get().await()
        val products = snapshot.documents.mapNotNull { it.toObject(ProductRequest::class.java) }
        Result.success(products)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun updateProductInFirestore(product: ProductRequest): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val db = Firebase.firestore
        db.collection("products").document(product.id).set(product).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 