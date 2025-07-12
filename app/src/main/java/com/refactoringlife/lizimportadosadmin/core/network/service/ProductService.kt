package com.refactoringlife.lizimportados.core.network.service

import com.refactoringlife.lizimportados.core.network.fireStore.FireStoreResponse
import com.refactoringlife.lizimportados.features.home.data.model.ProductModel
import kotlinx.coroutines.flow.Flow

class ProductService(
    private val firebaseService: FireBaseService = FireBaseService()
) {
    
    // Obtener todos los productos
    fun getProducts(): Flow<FireStoreResponse<List<ProductModel>>> {
        return firebaseService.getCollection("products", ProductModel::class.java)
    }
    
    // Obtener un producto específico
    fun getProduct(productId: String): Flow<FireStoreResponse<ProductModel>> {
        return firebaseService.getDocument("products", productId, ProductModel::class.java)
    }
    
    // Agregar un producto
    fun addProduct(product: ProductModel): Flow<FireStoreResponse<String>> {
        return firebaseService.addDocument("products", product)
    }
    
    // Actualizar un producto
    fun updateProduct(productId: String, product: ProductModel): Flow<FireStoreResponse<Unit>> {
        return firebaseService.updateDocument("products", productId, product)
    }
    
    // Eliminar un producto
    fun deleteProduct(productId: String): Flow<FireStoreResponse<Unit>> {
        return firebaseService.deleteDocument("products", productId)
    }
}