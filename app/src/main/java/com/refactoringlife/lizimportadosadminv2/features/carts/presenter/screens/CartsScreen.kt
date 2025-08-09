package com.refactoringlife.lizimportadosadminv2.features.carts.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import com.refactoringlife.lizimportadosadminv2.core.dto.response.CartResponse
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color

@Composable
fun CartsScreen(
    onNavigateToCartDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var carts by remember { mutableStateOf<List<CartResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val db = remember { Firebase.firestore }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("carts").get().await()
            carts = snapshot.documents.mapNotNull { doc ->
                try {
                    CartResponse(
                        email = doc.getString("email") ?: "",
                        lastUpdated = doc.getLong("lastUpdated") ?: 0,
                        productIds = (doc.get("productIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        status = doc.getString("status") ?: "AVAILABLE",
                        comboIds = (doc.get("comboIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.lastUpdated }
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
                text = "Carritos de Compra",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateBack) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
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
            val filteredCarts = carts.filter { 
                it.email.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCarts) { cart ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCartDetail(cart.email) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = cart.email, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Productos: ${cart.productIds.size + cart.comboIds.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Estado: ${if (cart.status == "AVAILABLE") "Activo" else "Procesado"}",
                                    color = if (cart.status == "AVAILABLE") Color.Green else Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = if (cart.status == "AVAILABLE") Color.Green else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}