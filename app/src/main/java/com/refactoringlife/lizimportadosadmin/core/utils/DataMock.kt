package com.refactoringlife.lizimportados.core.utils

import com.refactoringlife.lizimportados.core.dto.response.ConfigResponse
import com.refactoringlife.lizimportados.features.home.data.model.CombosModel
import com.refactoringlife.lizimportados.features.home.data.model.ProductModel

fun getProductsMock() = listOf(
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "Chaqueta de Jeans de exelente \u2028calidad. Especialpara salidas casuales",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "1"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "2"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "3"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "4"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "5"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "6"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "7"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "8"
    ),
    ProductModel(
        images = listOf(
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
            "https://i.postimg.cc/6q7WXnwC/pngwing-com.png"),
        name = "campera jean",
        description = "hermosa campera",
        oldPrice = "$55000",
        price = "$34000",
        brand = "Gap",
        size = "XL",
        id = "9"
    ),
)

fun getConfigMock() = ConfigResponse(
    listOf(
        ConfigResponse.Combo(
            showCombo = true,
            comboID = listOf("")
        )
    ),
    circleOptions = listOf("invierno", "verano", "ositos", "primavera"),
    weeklyOffers = true

)

fun getComboMock() = CombosModel(
    listOf(
        CombosModel.ComboModel(
            oldPrice = 55000, price = 22000,
            firstProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "1"
            ),
            secondProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "2"
            )
        ), CombosModel.ComboModel(
            oldPrice = 55000, price = 22000,
            firstProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "1"
            ),
            secondProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "2"
            )
        ), CombosModel.ComboModel(
            oldPrice = 55000, price = 22000,
            firstProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "1"
            ),
            secondProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "2"
            )
        ), CombosModel.ComboModel(
            oldPrice = 55000, price = 22000,
            firstProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "1"
            ),
            secondProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "2"
            )
        ), CombosModel.ComboModel(
            oldPrice = 55000, price = 22000,
            firstProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "1"
            ),
            secondProduct = CombosModel.ComboProductModel(
                image = "https://i.postimg.cc/6q7WXnwC/pngwing-com.png",
                description = "campera jean",
                brand = "Adidas",
                id = "2"
            )
        )
    )
)
