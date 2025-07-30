package com.refactoringlife.lizimportadosadminv2.core.utils

import com.refactoringlife.lizimportadosadminv2.core.dto.request.ProductRequest
import com.refactoringlife.lizimportadosadminv2.core.dto.response.ProductResponse
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Convierte un ProductRequest a un Map para Firestore, asegurando el mapeo correcto de campos booleanos
 */
fun ProductRequest.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "description" to description,
    "brand" to brand,
    "size" to size,
    "categories" to (categories ?: emptyList()),
    "combo_ids" to (comboIds ?: emptyList()),
    "gender" to gender,
    "images" to images,
    "is_available" to (isAvailable ?: false),
    "is_offer" to (isOffer ?: false),
    "offer_price" to (offerPrice ?: 0),
    "price" to price,
    "vendidos" to 0
)

/**
 * Convierte un DocumentSnapshot a ProductResponse, manejando correctamente los campos booleanos
 */
fun DocumentSnapshot.toProductResponse(): ProductResponse? {
    return try {
        ProductResponse(
            id = getString("id") ?: id,
            name = getString("name"),
            description = getString("description"),
            brand = getString("brand"),
            size = getString("size"),
            categories = (get("categories") as? List<*>)?.mapNotNull { it as? String },
            comboIds = (get("combo_ids") as? List<*>)?.mapNotNull { it as? String },
            gender = getString("gender"),
            images = (get("images") as? List<*>)?.mapNotNull { it as? String },
            isAvailable = getBoolean("is_available") ?: false,
            isOffer = getBoolean("is_offer") ?: false,
            offerPrice = getLong("offer_price")?.toInt() ?: 0,
            price = getLong("price")?.toInt()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convierte un Map<String, Any?> a ProductResponse, manejando correctamente los campos booleanos
 */
@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toProductResponse(): ProductResponse? {
    return try {
        ProductResponse(
            id = this["id"] as? String ?: "",
            name = this["name"] as? String,
            description = this["description"] as? String,
            brand = this["brand"] as? String,
            size = this["size"] as? String,
            categories = (this["categories"] as? List<*>)?.mapNotNull { it as? String },
            comboIds = (this["combo_ids"] as? List<*>)?.mapNotNull { it as? String },
            gender = this["gender"] as? String,
            images = (this["images"] as? List<*>)?.mapNotNull { it as? String },
            isAvailable = this["is_available"] as? Boolean ?: false,
            isOffer = this["is_offer"] as? Boolean ?: false,
            offerPrice = (this["offer_price"] as? Number)?.toInt() ?: 0,
            price = (this["price"] as? Number)?.toInt()
        )
    } catch (e: Exception) {
        null
    }
} 