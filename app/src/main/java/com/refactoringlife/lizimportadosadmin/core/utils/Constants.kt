package com.refactoringlife.lizimportadosadmin.core.utils

object ProductConstants {
    const val SELECT_OPTION = "Seleccione una opción"

    val CATEGORIES = listOf(
        SELECT_OPTION,
        "Camperas",
        "Busos",
        "Pantalones",
        "Polleras",
        "Zapatos",
        "Zapatillas",
        "Remeras",
        "Bodies",
        "Chalecos"
    )

    val GENDERS = listOf(
        SELECT_OPTION,
        "Hombre",
        "Mujer",
        "Niño",
        "Bebe"
    )

    fun getValueOrEmpty(value: String): String {
        return if (value == SELECT_OPTION) "" else value
    }
} 