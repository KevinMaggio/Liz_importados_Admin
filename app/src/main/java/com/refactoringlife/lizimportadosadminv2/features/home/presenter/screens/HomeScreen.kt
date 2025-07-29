package com.refactoringlife.lizimportadosadminv2.features.home.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.refactoringlife.lizimportadosadminv2.core.navigator.AppRoutes
import com.refactoringlife.lizimportadosadminv2.features.home.presenter.views.HomeDataView

@Composable
fun HomeScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToCreateCombo: () -> Unit,
    onNavigateToManageCombos: () -> Unit,
    onNavigateToSelectProductForEdit: () -> Unit,
    onNavigateToEditProductDetail: (String) -> Unit,
    onNavigateToDeleteProduct: () -> Unit,
    onNavigateToVenderProducto: () -> Unit,
    onNavigateToConfigApp: () -> Unit
) {
    HomeDataView(
        onNavigateToAddProduct = onNavigateToAddProduct,
        onNavigateToCreateCombo = onNavigateToCreateCombo,
        onNavigateToManageCombos = onNavigateToManageCombos,
        onNavigateToSelectProductForEdit = onNavigateToSelectProductForEdit,
        onNavigateToEditProductDetail = onNavigateToEditProductDetail,
        onNavigateToDeleteProduct = onNavigateToDeleteProduct,
        onNavigateToVenderProducto = onNavigateToVenderProducto,
        onNavigateToConfigApp = onNavigateToConfigApp
    )
}