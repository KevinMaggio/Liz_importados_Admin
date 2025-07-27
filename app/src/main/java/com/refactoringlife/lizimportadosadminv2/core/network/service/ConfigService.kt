package com.refactoringlife.lizimportadosadminv2.core.network.service

import com.google.firebase.firestore.FirebaseFirestore
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ConfigRequest
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ConfigResponse
import kotlinx.coroutines.tasks.await

class ConfigService {
    private val db = FirebaseFirestore.getInstance()
    private val configCollection = "config"
    
    suspend fun saveConfig(configRequest: ConfigRequest): Result<Unit> {
        return try {
            val configData = mapOf(
                "is_offers" to configRequest.isOffers,
                "options" to configRequest.circleOptions.map { option ->
                    mapOf(
                        "name" to option.name,
                        "image" to option.image
                    )
                },
                "has_combos" to configRequest.combos
            )
            
            db.collection(configCollection)
                .document("general")
                .set(configData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getConfig(): Result<ConfigResponse> {
        return try {
            val document = db.collection(configCollection)
                .document("general")
                .get()
                .await()
            
            if (document.exists()) {
                val isOffers = document.getBoolean("is_offers") ?: false
                val hasCombos = document.getBoolean("has_combos") ?: false
                val optionsData = document.get("options") as? List<Map<String, Any>> ?: emptyList()
                
                val options = optionsData.map { optionData ->
                    ConfigResponse.Option(
                        name = optionData["name"] as? String ?: "",
                        image = optionData["image"] as? String ?: ""
                    )
                }
                
                Result.success(ConfigResponse(
                    isOffers = isOffers,
                    circleOptions = options,
                    combos = hasCombos
                ))
            } else {
                // Retornar configuración por defecto si no existe
                Result.success(ConfigResponse())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 