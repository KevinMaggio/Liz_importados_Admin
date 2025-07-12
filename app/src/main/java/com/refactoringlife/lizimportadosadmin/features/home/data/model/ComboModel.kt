package com.refactoringlife.lizimportados.features.home.data.model

class CombosModel(
    val combos: List<ComboModel>
) {
    data class ComboModel(
        val oldPrice: Int,
        val price:Int,
        val firstProduct: ComboProductModel,
        val secondProduct: ComboProductModel
    )

    data class ComboProductModel(
        val id: String?,
        val brand: String?,
        val description: String,
        val image: String
    )
}