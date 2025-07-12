package com.refactoringlife.lizimportados.core.dto.request

import kotlinx.serialization.SerialName

data class ProductRequest (
    @SerialName("brand")
    val brand: String?,
    @SerialName("category")
    val category: String?,
    @SerialName("combo_id")
    val comboId: List<String>?,
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
    @SerialName("old_price")
    val oldPrice: Int?,
    @SerialName("price")
    val price: Int?,
    @SerialName("season")
    val season: String?,
    @SerialName("subtitle")
    val subtitle: String?,
    @SerialName("title")
    val title: String?,
    @SerialName("circle_option_filter")
    val circleOptionFilter: String?)