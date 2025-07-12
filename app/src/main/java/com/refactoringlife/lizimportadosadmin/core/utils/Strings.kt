package com.refactoringlife.lizimportados.core.utils

inline fun <R> String?.onValid(block: (String) -> R): R? {
    return if (!this.isNullOrBlank()) block(this) else null
}

fun String.capitalizeWords(): String =
    this.lowercase()
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }