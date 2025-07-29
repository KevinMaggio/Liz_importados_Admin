package com.refactoringlife.lizimportadosadminv2.core.dto.response

import kotlinx.serialization.SerialName

data class ConfigResponse(
    @SerialName("is_offers")
    val isOffers: Boolean = false,
    @SerialName("options")
    val circleOptions: List<Option> = emptyList(),
    @SerialName("has_combos")
    val combos: Boolean = false,
    // Aquí se pueden agregar más flags en el futuro
) {
    data class Option(
        @SerialName("name")
        val name: String,
        @SerialName("image")
        val image: String
    )
}