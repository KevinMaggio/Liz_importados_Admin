package com.refactoringlife.lizimportadosadminv2.core.utils

object ProductConstants {
    const val SELECT_OPTION = "Selecciona una opción"
    
    val CATEGORIES = listOf(
        "Pantalón",
        "Camisa",
        "Campera",
        "Remera",
        "Vestido",
        "Falda",
        "Short",
        "Buzo",
        "Sweater",
        "Chaleco",
        "Traje",
        "Blazer",
        "Jeans",
        "Bermuda",
        "Top",
        "Blusa",
        "Cardigan",
        "Abrigo",
        "Gorro",
        "Bufanda",
        "Guantes",
        "Calcetines",
        "Ropa Interior",
        "Pijama",
        "Ropa Deportiva",
        "Ropa Formal",
        "Ropa Casual",
        "Ropa de Fiesta",
        "Ropa de Trabajo",
        "Ropa de Playa",
        "Ropa de Invierno",
        "Ropa de Verano",
        "Ropa de Otoño",
        "Ropa de Primavera",
        "Ropa Juvenil",
        "Ropa Adulta",
        "Ropa Infantil",
        "Accesorios",
        "Calzado",
        "Bolsos",
        "Cinturones",
        "Relojes",
        "Joyas",
        "Usado",
        "Nuevo",
        "Hombre",
        "Niño",
        "Mujer"
    )
    
    val GENDERS = listOf(
        "Masculino",
        "Femenino",
        "Unisex",
        "Niño",
        "Niña"
    )
    
    fun getValueOrEmpty(value: String?): String {
        return if (value == null || value == SELECT_OPTION) "" else value
    }
} 