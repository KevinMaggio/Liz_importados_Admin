package com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

data class ComboListItem(
    val comboId: Int,
    val oldPrice: Int,
    val newPrice: Int,
    val product1Name: String,
    val product2Name: String,
    val isAvailable: Boolean
)

@Composable
fun ManageCombosScreen(
    onNavigateToCreateCombo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var combos by remember { mutableStateOf<List<ComboListItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ComboListItem?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar combos
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                loading = true
                val db = Firebase.firestore
                val snapshot = db.collection("combos").get().await()
                
                val comboList = mutableListOf<ComboListItem>()
                
                for (doc in snapshot.documents) {
                    try {
                        val comboId = doc.getLong("comboId")?.toInt() ?: continue
                        val oldPrice = doc.getLong("oldPrice")?.toInt() ?: 0
                        val newPrice = doc.getLong("newPrice")?.toInt() ?: 0
                        val product1Id = doc.getString("product1Id") ?: continue
                        val product2Id = doc.getString("product2Id") ?: continue
                        val isAvailable = doc.getBoolean("isAvailable") ?: true
                        
                        // Obtener nombres de productos
                        val product1Doc = db.collection("products").document(product1Id).get().await()
                        val product2Doc = db.collection("products").document(product2Id).get().await()
                        
                        val product1Name = product1Doc.getString("name") ?: "Producto no encontrado"
                        val product2Name = product2Doc.getString("name") ?: "Producto no encontrado"
                        
                        comboList.add(
                            ComboListItem(
                                comboId = comboId,
                                oldPrice = oldPrice,
                                newPrice = newPrice,
                                product1Name = product1Name,
                                product2Name = product2Name,
                                isAvailable = isAvailable
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("ManageCombosScreen", "Error procesando combo: ${e.message}")
                    }
                }
                
                combos = comboList.sortedBy { it.comboId }
                error = null
            } catch (e: Exception) {
                error = e.message
                Log.e("ManageCombosScreen", "Error cargando combos: ${e.message}")
            } finally {
                loading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título y botón crear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gestionar Combos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onNavigateToCreateCombo
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Combo")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay combos creados")
                }
            }
            else -> {
                LazyColumn {
                    items(combos) { combo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Combo #${combo.comboId}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text("${combo.product1Name} + ${combo.product2Name}")
                                        Text(
                                            text = "Precio original: $${combo.oldPrice} → Combo: $${combo.newPrice}",
                                            color = Color.Gray
                                        )
                                        if (!combo.isAvailable) {
                                            Text(
                                                text = "❌ NO DISPONIBLE",
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    if (combo.isAvailable) {
                                        IconButton(
                                            onClick = { showDeleteDialog = combo }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Dar de baja",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de confirmación para dar de baja
    showDeleteDialog?.let { combo ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dar de baja Combo #${combo.comboId}")
                }
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres dar de baja este combo?\n\n" +
                    "Esta acción:\n" +
                    "• Marcará el combo como no disponible\n" +
                    "• Removerá el combo de los productos involucrados\n" +
                    "• No se puede deshacer"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val db = Firebase.firestore
                                
                                // Marcar combo como no disponible
                                db.collection("combos")
                                    .document(combo.comboId.toString())
                                    .update("isAvailable", false)
                                    .await()
                                
                                // Remover comboId de los productos
                                val snapshot = db.collection("products").get().await()
                                for (doc in snapshot.documents) {
                                    val comboIds = doc.get("combo_ids") as? List<String> ?: emptyList()
                                    if (comboIds.contains(combo.comboId.toString())) {
                                        val newComboIds = comboIds.filter { it != combo.comboId.toString() }
                                        doc.reference.update("combo_ids", newComboIds).await()
                                    }
                                }
                                
                                // Recargar combos
                                val newSnapshot = db.collection("combos").get().await()
                                val comboList = mutableListOf<ComboListItem>()
                                
                                for (doc in newSnapshot.documents) {
                                    try {
                                        val comboId = doc.getLong("comboId")?.toInt() ?: continue
                                        val oldPrice = doc.getLong("oldPrice")?.toInt() ?: 0
                                        val newPrice = doc.getLong("newPrice")?.toInt() ?: 0
                                        val product1Id = doc.getString("product1Id") ?: continue
                                        val product2Id = doc.getString("product2Id") ?: continue
                                        val isAvailable = doc.getBoolean("isAvailable") ?: true
                                        
                                        val product1Doc = db.collection("products").document(product1Id).get().await()
                                        val product2Doc = db.collection("products").document(product2Id).get().await()
                                        
                                        val product1Name = product1Doc.getString("name") ?: "Producto no encontrado"
                                        val product2Name = product2Doc.getString("name") ?: "Producto no encontrado"
                                        
                                        comboList.add(
                                            ComboListItem(
                                                comboId = comboId,
                                                oldPrice = oldPrice,
                                                newPrice = newPrice,
                                                product1Name = product1Name,
                                                product2Name = product2Name,
                                                isAvailable = isAvailable
                                            )
                                        )
                                    } catch (e: Exception) {
                                        Log.e("ManageCombosScreen", "Error procesando combo: ${e.message}")
                                    }
                                }
                                
                                combos = comboList.sortedBy { it.comboId }
                                showDeleteDialog = null
                            } catch (e: Exception) {
                                error = e.message
                                Log.e("ManageCombosScreen", "Error dando de baja combo: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Dar de baja")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 