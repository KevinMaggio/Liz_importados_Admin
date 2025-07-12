package com.refactoringlife.lizimportados.core.navigator

import androidx.navigation.NavController
import com.refactoringlife.lizimportadosadmin.core.navigator.AppRoutes

/**
 * Función utilitaria para navegar desde el BottomBar
 * Mantiene HOME como base y reemplaza pantallas en lugar de agregarlas al back stack
 */
fun NavController.navigateFromBottomBar(destination: String) {
    navigate(destination) {
        // Pop up to HOME (inclusive = false) para mantener HOME como base
        popUpTo(AppRoutes.HOME) { inclusive = false }
        // Evita múltiples instancias de la misma pantalla
        launchSingleTop = true
        // Restaura el estado si la pantalla ya existe
        restoreState = true
    }
}

/**
 * Función utilitaria para navegar a detalles desde HOME
 * Mantiene HOME como base para poder volver
 */
fun NavController.navigateToDetails(filter: String, id: String) {
    navigate("details/$filter/$id") {
        launchSingleTop = true
        popUpTo(AppRoutes.HOME) { inclusive = false } // HOME queda como base
    }
}

/**
 * Función utilitaria para navegar desde LOGIN a HOME
 * Elimina LOGIN del back stack
 */
fun NavController.navigateFromLoginToHome() {
    navigate(AppRoutes.HOME) {
        popUpTo(AppRoutes.LOGIN) { inclusive = true } // elimina LOGIN del backstack
        launchSingleTop = true
    }
} 