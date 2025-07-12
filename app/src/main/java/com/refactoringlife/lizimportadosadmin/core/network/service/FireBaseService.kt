package com.refactoringlife.lizimportados.core.network.service

import com.google.firebase.firestore.FirebaseFirestore
import com.refactoringlife.lizimportados.core.network.fireStore.FireStoreResponse
import com.refactoringlife.lizimportados.core.network.fireStore.FirebaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class FireBaseService(
    private val firestore: FirebaseFirestore = FirebaseProvider.instance
) {
    // Obtener un documento específico
    fun <T> getDocument(
        collectionPath: String,
        documentId: String,
        dataClass: Class<T>
    ): Flow<FireStoreResponse<T>> = flow {
        emit(FireStoreResponse.Loading)
        try {
            val snapshot = firestore.collection(collectionPath).document(documentId).get().await()
            val data = snapshot.toObject(dataClass)
            if (data != null) {
                emit(FireStoreResponse.Success(data))
            } else {
                emit(FireStoreResponse.Error("Documento vacío o no encontrado"))
            }
        } catch (e: Exception) {
            emit(FireStoreResponse.Error("Error al obtener documento", e))
        }
    }.flowOn(Dispatchers.IO)

    // Obtener toda una colección
    fun <T> getCollection(
        collectionPath: String,
        dataClass: Class<T>
    ): Flow<FireStoreResponse<List<T>>> = flow {
        emit(FireStoreResponse.Loading)
        try {
            val snapshot = firestore.collection(collectionPath).get().await()
            val data = snapshot.documents.mapNotNull { doc ->
                doc.toObject(dataClass)
            }
            emit(FireStoreResponse.Success(data))
        } catch (e: Exception) {
            emit(FireStoreResponse.Error("Error al obtener colección", e))
        }
    }.flowOn(Dispatchers.IO)

    // Agregar un documento
    fun <T> addDocument(
        collectionPath: String,
        data: T
    ): Flow<FireStoreResponse<String>> = flow {
        emit(FireStoreResponse.Loading)
        try {
            val documentReference = firestore.collection(collectionPath).add(data as Any).await()
            emit(FireStoreResponse.Success(documentReference.id))
        } catch (e: Exception) {
            emit(FireStoreResponse.Error("Error al agregar documento", e))
        }
    }.flowOn(Dispatchers.IO)

    // Actualizar un documento
    fun <T> updateDocument(
        collectionPath: String,
        documentId: String,
        data: T
    ): Flow<FireStoreResponse<Unit>> = flow {
        emit(FireStoreResponse.Loading)
        try {
            firestore.collection(collectionPath).document(documentId).set(data as Any).await()
            emit(FireStoreResponse.Success(Unit))
        } catch (e: Exception) {
            emit(FireStoreResponse.Error("Error al actualizar documento", e))
        }
    }.flowOn(Dispatchers.IO)

    // Eliminar un documento
    fun deleteDocument(
        collectionPath: String,
        documentId: String
    ): Flow<FireStoreResponse<Unit>> = flow {
        emit(FireStoreResponse.Loading)
        try {
            firestore.collection(collectionPath).document(documentId).delete().await()
            emit(FireStoreResponse.Success(Unit))
        } catch (e: Exception) {
            emit(FireStoreResponse.Error("Error al eliminar documento", e))
        }
    }.flowOn(Dispatchers.IO)
}