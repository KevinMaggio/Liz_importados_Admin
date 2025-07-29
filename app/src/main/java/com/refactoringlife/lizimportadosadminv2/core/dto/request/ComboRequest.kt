package com.refactoringlife.lizimportadosadminv2.core.dto.request

data class ComboRequest(
    val comboId: Int, // ID numérico automático
    val oldPrice: Int, // Suma de precios originales
    val newPrice: Int, // Precio del combo (usuario)
    val product1Id: String,
    val product2Id: String,
    val isAvailable: Boolean = true
) {
    // Constructor sin argumentos para Firestore
    constructor() : this(
        comboId = 0,
        oldPrice = 0,
        newPrice = 0,
        product1Id = "",
        product2Id = "",
        isAvailable = true
    )
}