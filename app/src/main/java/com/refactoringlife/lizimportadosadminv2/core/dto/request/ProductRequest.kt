package com.refactoringlife.lizimportadosadminv2.core.dto.request

data class ProductRequest(
    val id: String = "",
    val name: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val size: String? = "",
    val category: String? = null,
    val comboIds: List<String>? = null,
    val comboPrice: Int? = null,
    val gender: String? = null,
    val images: List<String>? = null,
    val isAvailable: Boolean? = null,
    val isOffer: Boolean? = null,
    val offerPrice: Int = 0,
    val price: Int? = null
) {
    // Constructor sin argumentos para Firestore
    constructor() : this(
        id = "",
        name = null,
        description = null,
        brand = null,
        size = "",
        category = null,
        comboIds = null,
        comboPrice = null,
        gender = null,
        images = null,
        isAvailable = null,
        isOffer = null,
        offerPrice = 0,
        price = null
    )
}