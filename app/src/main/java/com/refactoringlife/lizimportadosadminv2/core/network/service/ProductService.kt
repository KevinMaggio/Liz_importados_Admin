package com.refactoringlife.lizimportadosadminv2.core.network.service

import com.refactoringlife.lizimportados.core.network.fireStore.FireStoreResponse
import com.refactoringlife.lizimportados.core.network.service.FireBaseService
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ProductRequest
import kotlinx.coroutines.flow.Flow

class ProductService(
    private val firebaseService: FireBaseService = FireBaseService()
) {
    
    // Obtener todos los productos
    fun getProducts(): Flow<FireStoreResponse<List<ProductResponse>>> {
        return firebaseService.getCollection("products", ProductResponse::class.java)
    }
    
    // Obtener un producto específico
    fun getProduct(productId: String): Flow<FireStoreResponse<ProductResponse>> {
        return firebaseService.getDocument("products", productId, ProductResponse::class.java)
    }
    
    // Agregar un producto
    fun addProduct(product: ProductRequest): Flow<FireStoreResponse<String>> {
        return firebaseService.addDocument("products", product)
    }
    
    // Actualizar un producto
    fun updateProduct(productId: String, product: ProductRequest): Flow<FireStoreResponse<Unit>> {
        return firebaseService.updateDocument("products", productId, product)
    }
    
    // Eliminar un producto
    fun deleteProduct(productId: String): Flow<FireStoreResponse<Unit>> {
        return firebaseService.deleteDocument("products", productId)
    }
}