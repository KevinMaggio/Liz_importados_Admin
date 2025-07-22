package com.refactoringlife.lizimportadosadmin.features.home.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.refactoringlife.lizimportadosadmin.core.navigator.AppRoutes
import com.refactoringlife.lizimportadosadmin.features.home.presenter.views.HomeDataView

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {
    HomeDataView(
        modifier = modifier,
        onNavigateToAddProduct = {
            navController?.navigate("add_product")
        },
        onNavigateToEditProduct = {
            navController?.navigate("edit_product_select")
        },
        onNavigateToCreateCombo = {
            navController?.navigate("create_combo")
        },
        onNavigateToDeleteProduct = {
            navController?.navigate("delete_product")
        },
        onNavigateToVenderProduct = {
            navController?.navigate("vender_producto")
        },
        onNavigateToTestImageProcessing = {
            navController?.navigate(AppRoutes.TEST_IMAGE_PROCESSING)
        }
    )
}