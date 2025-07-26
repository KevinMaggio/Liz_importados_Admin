package com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ProductRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CreateComboScreen() {
    val coroutineScope = rememberCoroutineScope()
    val (loading, setLoading) = remember { mutableStateOf(true) }
    val (products, setProducts) = remember { mutableStateOf<List<ProductRequest>>(emptyList()) }
    val (selectedProducts, setSelectedProducts) = remember { mutableStateOf<List<ProductRequest>>(emptyList()) }
    val (comboPrice, setComboPrice) = remember { mutableStateOf("") }
    val (success, setSuccess) = remember { mutableStateOf<String?>(null) }
    val (error, setError) = remember { mutableStateOf<String?>(null) }

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
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Selecciona 2 productos para el combo", modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        val imageUrl = product.images?.firstOrNull()
                        val isSelected = selectedProducts.any { it.id == product.id }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
                                .clickable(enabled = selectedProducts.size < 2 || isSelected) {
                                    setSelectedProducts(
                                        if (isSelected) selectedProducts.filter { it.id != product.id }
                                        else if (selectedProducts.size < 2) selectedProducts + product
                                        else selectedProducts
                                    )
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
                                if (isSelected) {
                                    Text("  ✓", color = Color(0xFF00796B))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comboPrice,
                    onValueChange = setComboPrice,
                    label = { Text("Precio del Combo") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedProducts.size == 2
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val comboID = UUID.randomUUID().toString()
                            val price = comboPrice.toIntOrNull()
                            if (selectedProducts.size == 2 && price != null) {
                                val update1 = updateProductCombo(selectedProducts[0], comboID, price)
                                val update2 = updateProductCombo(selectedProducts[1], comboID, price)
                                if (update1.isSuccess && update2.isSuccess) {
                                    setSuccess("Combo creado con éxito")
                                    setSelectedProducts(emptyList())
                                    setComboPrice("")
                                    // Recargar productos
                                    setLoading(true)
                                    val reload = getAllProducts()
                                    setProducts(reload.getOrNull() ?: emptyList())
                                    setLoading(false)
                                } else {
                                    setError(update1.exceptionOrNull()?.message ?: update2.exceptionOrNull()?.message)
                                }
                            }
                        }
                    },
                    enabled = selectedProducts.size == 2 && comboPrice.isNotBlank()
                ) {
                    Text("Crear Combo")
                }
                if (success != null) {
                    Text(success, color = Color(0xFF388E3C), modifier = Modifier.padding(top = 16.dp))
                }
                if (error != null) {
                    Text("Error: $error", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}

suspend fun updateProductCombo(product: ProductRequest, comboID: String, comboPrice: Int): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val db = Firebase.firestore
        val newComboIds = (product.comboId?.toMutableList() ?: mutableListOf()).apply { add(comboID) }
        val updated = product.copy(
            comboId = newComboIds,
            comboPrice = comboPrice
        )
        db.collection("products").document(product.id).set(updated).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
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