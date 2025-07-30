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
        "Interior",
        "Pijama",
        "Deportiva",
        "Formal",
        "Casual",
        "Fiesta",
        "Trabajo",
        "Playa",
        "Invierno",
        "Verano",
        "Otoño",
        "Primavera",
        "Juvenil",
        "Adulta",
        "Infantil",
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
        "Hombre",
        "Mujer",
        "Niño",
    )
    
    fun getValueOrEmpty(value: String?): String {
        return if (value == null || value == SELECT_OPTION) "" else value
    }
} 