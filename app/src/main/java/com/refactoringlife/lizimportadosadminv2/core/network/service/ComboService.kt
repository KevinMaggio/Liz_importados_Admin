package com.refactoringlife.lizimportadosadminv2.core.network.service

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ComboService {
    private val db = FirebaseFirestore.getInstance()
    private val combosCollection = "combos"
    private val configCollection = "config"
    
    /**
     * Obtiene el siguiente ID secuencial para un combo
     * Busca el último combo creado y asigna el siguiente número
     */
    suspend fun getNextComboId(): Int {
        return try {
            // Obtener todos los combos ordenados por comboId descendente
            val snapshot = db.collection(combosCollection)
                .orderBy("comboId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                // Si no hay combos, empezar con 1
                1
            } else {
                // Obtener el último comboId y sumar 1
                val lastComboId = snapshot.documents.first().getLong("comboId")?.toInt() ?: 0
                lastComboId + 1
            }
        } catch (e: Exception) {
            // En caso de error, usar timestamp como fallback
            (System.currentTimeMillis() % 10000).toInt() + 1
        }
    }
    
    /**
     * Guarda un combo con ID secuencial
     */
    suspend fun saveCombo(comboRequest: com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest): Result<Unit> {
        return try {
            // Asignar ID secuencial si no tiene uno
            val comboWithId = if (comboRequest.comboId == 0) {
                val nextId = getNextComboId()
                comboRequest.copy(comboId = nextId)
            } else {
                comboRequest
            }
            
            // Guardar en Firestore
            db.collection(combosCollection)
                .document(comboWithId.comboId.toString())
                .set(comboWithId)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los combos activos
     */
    suspend fun getActiveCombos(): Result<List<com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest>> {
        return try {
            val snapshot = db.collection(combosCollection)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
            
            val combos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest::class.java)
            }
            
            Result.success(combos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los combos (activos e inactivos)
     */
    suspend fun getAllCombos(): Result<List<com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest>> {
        return try {
            val snapshot = db.collection(combosCollection)
                .get()
                .await()
            
            val combos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.refactoringlife.lizimportadosadminv2.core.dto.request.ComboRequest::class.java)
            }
            
            Result.success(combos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 