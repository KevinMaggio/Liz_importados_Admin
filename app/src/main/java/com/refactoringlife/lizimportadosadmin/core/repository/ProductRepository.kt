package com.refactoringlife.lizimportados.core.repository

import android.util.Log
import com.refactoringlife.lizimportados.core.network.fireStore.FireStoreResponse
import com.refactoringlife.lizimportados.core.network.service.ProductService
import com.refactoringlife.lizimportados.features.home.data.model.ProductModel
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productService: ProductService = ProductService()
) {
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    // Obtener todos los productos
    fun getProducts(): Flow<FireStoreResponse<List<ProductModel>>> {
        Log.d(TAG, "🔄 Iniciando obtención de productos desde Firestore")
        return productService.getProducts()
    }
    
    // Obtener un producto específico
    fun getProduct(productId: String): Flow<FireStoreResponse<ProductModel>> {
        Log.d(TAG, "🔄 Obteniendo producto con ID: $productId")
        return productService.getProduct(productId)
    }
    
    // Agregar un producto
    fun addProduct(product: ProductModel): Flow<FireStoreResponse<String>> {
        Log.d(TAG, "➕ Agregando nuevo producto: ${product.name}")
        return productService.addProduct(product)
    }
    
    // Actualizar un producto
    fun updateProduct(productId: String, product: ProductModel): Flow<FireStoreResponse<Unit>> {
        Log.d(TAG, "✏️ Actualizando producto con ID: $productId")
        return productService.updateProduct(productId, product)
    }
    
    // Eliminar un producto
    fun deleteProduct(productId: String): Flow<FireStoreResponse<Unit>> {
        Log.d(TAG, "🗑️ Eliminando producto con ID: $productId")
        return productService.deleteProduct(productId)
    }
} 