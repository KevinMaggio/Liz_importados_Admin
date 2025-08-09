package com.refactoringlife.lizimportadosadminv2.core.dto.response

data class CartResponse(
    val email: String = "",
    val lastUpdated: Long = 0,
    val productIds: List<String> = emptyList(),
    val status: String = "AVAILABLE",
    val comboIds: List<String> = emptyList()
) {
    // Constructor sin argumentos para Firestore
    constructor() : this(
        email = "",
        lastUpdated = 0,
        productIds = emptyList(),
        status = "AVAILABLE",
        comboIds = emptyList()
    )
}