package com.refactoringlife.lizimportadosadminv2.core.repository

import android.util.Log
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ProductRequest
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.core.utils.toFirestoreMap
import com.refactoringlife.lizimportadosadminv2.core.utils.toProductResponse
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = Firebase.firestore
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    // Obtener todos los productos
    suspend fun getProducts(): List<ProductResponse> {
        Log.d(TAG, "🔄 Iniciando obtención de productos desde Firestore")
        return try {
            val snapshot = db.collection("products").get().await()
            snapshot.documents.mapNotNull { doc -> doc.toProductResponse() }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo productos: ${e.message}")
            emptyList()
        }
    }
    
    // Obtener un producto específico
    suspend fun getProduct(productId: String): ProductResponse? {
        Log.d(TAG, "🔄 Obteniendo producto con ID: $productId")
        return try {
            val doc = db.collection("products").document(productId).get().await()
            doc.toProductResponse()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo producto $productId: ${e.message}")
            null
        }
    }
    
    // Agregar un producto
    suspend fun addProduct(product: ProductRequest): Result<Unit> {
        Log.d(TAG, "➕ Agregando nuevo producto: ${product.name}")
        return try {
            db.collection("products")
                .document(product.id)
                .set(product.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error agregando producto: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Actualizar un producto
    suspend fun updateProduct(productId: String, updates: Map<String, Any>): Result<Unit> {
        Log.d(TAG, "✏️ Actualizando producto con ID: $productId")
        return try {
            db.collection("products")
                .document(productId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando producto: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Buscar productos por nombre
    suspend fun searchProductsByName(query: String): List<ProductResponse> {
        Log.d(TAG, "🔍 Buscando productos con nombre: $query")
        return try {
            val snapshot = if (query.isBlank()) {
                db.collection("products")
                    .whereEqualTo("is_available", true)
                    .get()
                    .await()
            } else {
                db.collection("products")
                    .whereEqualTo("is_available", true)
                    .whereGreaterThanOrEqualTo("name", query)
                    .whereLessThanOrEqualTo("name", query + '\uf8ff')
                    .get()
                    .await()
            }
            
            snapshot.documents.mapNotNull { doc -> doc.toProductResponse() }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error buscando productos: ${e.message}")
            emptyList()
        }
    }
    
    // Incrementar contador de ventas
    suspend fun incrementVentas(productId: String): Result<Unit> {
        Log.d(TAG, "📈 Incrementando ventas para producto: $productId")
        return try {
            val productRef = db.collection("products").document(productId)
            val productDoc = productRef.get().await()
            val ventasActuales = productDoc.getLong("vendidos")?.toInt() ?: 0
            productRef.update("vendidos", ventasActuales + 1).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error incrementando ventas: ${e.message}")
            Result.failure(e)
        }
    }
} 