package com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

data class ComboWithProducts(
    val id: Int,
    val oldPrice: Int,
    val newPrice: Int,
    val isAvailable: Boolean,
    val product1: ProductInCombo,
    val product2: ProductInCombo
)

data class ProductInCombo(
    val id: String,
    val name: String,
    val image: String?,
    val price: Int
)

@Composable
fun ManageCombosScreen(
    onNavigateBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var combos by remember { mutableStateOf<List<ComboWithProducts>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var selectedCombo by remember { mutableStateOf<ComboWithProducts?>(null) }
    var processingDeactivation by remember { mutableStateOf(false) }

    // Dialog de confirmación
    if (showDialog && selectedCombo != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Desactivación") },
            text = { 
                Text(
                    "¿Estás seguro que deseas desactivar el Combo #${selectedCombo?.id}?\n" +
                    "Esta acción también:\n" +
                    "• Desactivará los productos asociados\n" +
                    "• Eliminará el ID del combo de los productos"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            processingDeactivation = true
                            try {
                                val db = Firebase.firestore
                                
                                // 1. Desactivar el combo
                                db.collection("combos")
                                    .document(selectedCombo!!.id.toString())
                                    .update("is_available", false)
                                    .await()
                                
                                // 2. Desactivar productos y eliminar comboId
                                val batch = db.batch()
                                
                                // Producto 1
                                val product1Ref = db.collection("products")
                                    .document(selectedCombo!!.product1.id)
                                val product1Doc = product1Ref.get().await()
                                val comboIds1 = (product1Doc.get("combo_ids") as? List<String> ?: emptyList())
                                    .filter { it != selectedCombo!!.id.toString() }
                                batch.update(product1Ref, mapOf(
                                    "is_available" to false,
                                    "combo_ids" to comboIds1
                                ))
                                
                                // Producto 2
                                val product2Ref = db.collection("products")
                                    .document(selectedCombo!!.product2.id)
                                val product2Doc = product2Ref.get().await()
                                val comboIds2 = (product2Doc.get("combo_ids") as? List<String> ?: emptyList())
                                    .filter { it != selectedCombo!!.id.toString() }
                                batch.update(product2Ref, mapOf(
                                    "is_available" to false,
                                    "combo_ids" to comboIds2
                                ))
                                
                                batch.commit().await()
                                
                                // Actualizar la lista local
                                combos = combos.map { combo ->
                                    if (combo.id == selectedCombo!!.id) {
                                        combo.copy(isAvailable = false)
                                    } else {
                                        combo
                                    }
                                }
                                
                                showDialog = false
                                selectedCombo = null
                                
                            } catch (e: Exception) {
                                Log.e("ManageCombosScreen", "Error desactivando combo: ${e.message}")
                                error = "Error al desactivar el combo: ${e.message}"
                            } finally {
                                processingDeactivation = false
                            }
                        }
                    },
                    enabled = !processingDeactivation
                ) {
                    Text(if (processingDeactivation) "Procesando..." else "Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    enabled = !processingDeactivation
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val db = Firebase.firestore
            val combosList = mutableListOf<ComboWithProducts>()
            
            // 1. Obtener todos los combos
            val combosSnapshot = db.collection("combos").get().await()
            
            // 2. Para cada combo, obtener sus productos
            for (comboDoc in combosSnapshot.documents) {
                try {
                    val comboId = comboDoc.getLong("combo_id")?.toInt() ?: continue
                    val oldPrice = comboDoc.getLong("old_price")?.toInt() ?: 0
                    val newPrice = comboDoc.getLong("new_price")?.toInt() ?: 0
                    val isAvailable = comboDoc.getBoolean("is_available") ?: false
                    val product1Id = comboDoc.getString("product1_id") ?: continue
                    val product2Id = comboDoc.getString("product2_id") ?: continue
                    
                    // Obtener producto 1
                    val product1Doc = db.collection("products").document(product1Id).get().await()
                    val product1 = ProductInCombo(
                        id = product1Id,
                        name = product1Doc.getString("name") ?: "",
                        image = (product1Doc.get("images") as? List<*>)?.firstOrNull() as? String,
                        price = product1Doc.getLong("price")?.toInt() ?: 0
                    )
                    
                    // Obtener producto 2
                    val product2Doc = db.collection("products").document(product2Id).get().await()
                    val product2 = ProductInCombo(
                        id = product2Id,
                        name = product2Doc.getString("name") ?: "",
                        image = (product2Doc.get("images") as? List<*>)?.firstOrNull() as? String,
                        price = product2Doc.getLong("price")?.toInt() ?: 0
                    )
                    
                    combosList.add(
                        ComboWithProducts(
                            id = comboId,
                            oldPrice = oldPrice,
                            newPrice = newPrice,
                            isAvailable = isAvailable,
                            product1 = product1,
                            product2 = product2
                        )
                    )
                } catch (e: Exception) {
                    Log.e("ManageCombosScreen", "Error procesando combo: ${e.message}")
                    continue
                }
            }
            
            combos = combosList.sortedByDescending { it.id }
            error = null
        } catch (e: Exception) {
            Log.e("ManageCombosScreen", "Error cargando combos: ${e.message}")
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
        Text(
            text = "Gestionar Combos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            combos.isEmpty() -> {
                Text(
                    text = "No hay combos disponibles",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(combos) { combo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (combo.isAvailable) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    Color(0xFFEEEEEE)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Combo #${combo.id}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!combo.isAvailable) {
                                        Text(
                                            text = "Inactivo",
                                            color = Color.Red,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Producto 1
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    combo.product1.image?.let { image ->
                                        AsyncImage(
                                            model = image,
                                            contentDescription = "Producto 1",
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column {
                                        Text(text = combo.product1.name)
                                        Text(
                                            text = "$${combo.product1.price}",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "+",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                // Producto 2
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    combo.product2.image?.let { image ->
                                        AsyncImage(
                                            model = image,
                                            contentDescription = "Producto 2",
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column {
                                        Text(text = combo.product2.name)
                                        Text(
                                            text = "$${combo.product2.price}",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                // Precios
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Precio Original",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "$${combo.oldPrice}",
                                            fontSize = 16.sp,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Precio Combo",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "$${combo.newPrice}",
                                            fontSize = 16.sp,
                                            color = Color(0xFF388E3C)
                                        )
                                    }
                                }
                                
                                if (combo.isAvailable) {
                                    Button(
                                        onClick = {
                                            selectedCombo = combo
                                            showDialog = true
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFEF5350)
                                        )
                                    ) {
                                        Text("Desactivar Combo")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
} 