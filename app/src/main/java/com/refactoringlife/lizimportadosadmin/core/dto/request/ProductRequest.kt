package com.refactoringlife.lizimportadosadmin.core.dto.request

import kotlinx.serialization.SerialName

data class ProductRequest (
    @SerialName("id")
    val id : String,
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("brand")
    val brand: String?,
    @SerialName("category")
    val category: String?,
    @SerialName("combo_id")
    val comboId: List<String>? = null,
    @SerialName("combo_price")
    val comboPrice: Int?,
    @SerialName("gender")
    val gender: String?,
    @SerialName("images")
    val images: List<String>?,
    @SerialName("is_available")
    val isAvailable: Boolean?,
    @SerialName("is_offer")
    val isOffer: Boolean?,
    @SerialName("offer_price")
    val offerPrice: Int,
    @SerialName("price")
    val price: Int?,
    @SerialName("season")
    val season: String?,
    @SerialName("circle_option_filter")
    val circleOptionFilter: String?)