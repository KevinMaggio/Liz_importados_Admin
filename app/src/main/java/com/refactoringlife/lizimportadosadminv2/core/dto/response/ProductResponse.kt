package com.refactoringlife.lizimportadosadminv2.core.dto.response

import kotlinx.serialization.SerialName

data class ProductResponse(
    @SerialName("id")
    val id : String = "",
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("brand")
    val brand: String? = null,
    @SerialName("size")
    val size: String? = "",
    @SerialName("category")
    val category: String? = null,
    @SerialName("combo_ids")
    val comboIds: List<String>?,
    @SerialName("combo_price")
    val comboPrice: Int? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("images")
    val images: List<String>? = null,
    @SerialName("is_available")
    val isAvailable: Boolean? = null,
    @SerialName("is_offer")
    val isOffer: Boolean? = null,
    @SerialName("offer_price")
    val offerPrice: Int = 0,
    @SerialName("price")
    val price: Int? = null
)