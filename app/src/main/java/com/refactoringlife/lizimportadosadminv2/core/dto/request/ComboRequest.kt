package com.refactoringlife.lizimportadosadminv2.core.dto.request

data class ComboRequest(
    val comboId: String,
    val oldPrice: Int, // Suma de precios originales
    val newPrice: Int, // Precio del combo (usuario)
    val product1Id: String,
    val product2Id: String,
    val isAvailable: Boolean = true
)