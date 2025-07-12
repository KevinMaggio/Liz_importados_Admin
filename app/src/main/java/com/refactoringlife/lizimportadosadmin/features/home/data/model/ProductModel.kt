package com.refactoringlife.lizimportados.features.home.data.model

data class ProductModel(
    val id: String,
    val images: List<String>,
    val brand: String?,
    val name: String?,
    val size: String?,
    val description: String?,
    val oldPrice: String?,
    val price: String?,
)