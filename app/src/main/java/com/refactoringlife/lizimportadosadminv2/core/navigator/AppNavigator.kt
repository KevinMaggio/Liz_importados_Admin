package com.refactoringlife.lizimportadosadminv2.core.navigator

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.refactoringlife.lizimportadosadminv2.features.home.presenter.screens.HomeScreen
import com.refactoringlife.lizimportadosadminv2.features.login.presenter.screens.LoginScreen
import com.refactoringlife.lizimportadosadminv2.features.login.presenter.viewmodel.LoginViewModel
import com.refactoringlife.lizimportadosadminv2.features.addProduct.presenter.screens.AddProductScreen
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens.CreateComboScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.SelectProductForEditScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.EditProductDetailScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.DeleteProductScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.VenderProductoScreen
import com.refactoringlife.lizimportadosadminv2.features.config.presenter.screens.ConfigAppScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    onGoogleSignInClick: (Intent) -> Unit
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN,
        modifier = modifier
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onGoogleSignInClick = onGoogleSignInClick,
                onNavigateToHome = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(AppRoutes.HOME) {
            HomeScreen(
                onNavigateToAddProduct = { navController.navigate(AppRoutes.ADD_PRODUCT) },
                onNavigateToCreateCombo = { navController.navigate(AppRoutes.CREATE_COMBO) },
                onNavigateToSelectProductForEdit = { navController.navigate(AppRoutes.SELECT_PRODUCT_FOR_EDIT) },
                onNavigateToDeleteProduct = { navController.navigate(AppRoutes.DELETE_PRODUCT) },
                onNavigateToVenderProducto = { navController.navigate(AppRoutes.VENDER_PRODUCTO) },
                onNavigateToConfigApp = { navController.navigate(AppRoutes.CONFIG_APP) }
            )
        }
        
        composable(AppRoutes.ADD_PRODUCT) {
            AddProductScreen()
        }
        
        composable(AppRoutes.CREATE_COMBO) {
            CreateComboScreen()
        }
        
        composable(AppRoutes.SELECT_PRODUCT_FOR_EDIT) {
            SelectProductForEditScreen(
                onProductSelected = { productId ->
                    navController.navigate("${AppRoutes.EDIT_PRODUCT_DETAIL}/$productId")
                }
            )
        }
        
        composable(
            route = "${AppRoutes.EDIT_PRODUCT_DETAIL}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(AppRoutes.DELETE_PRODUCT) {
            DeleteProductScreen()
        }
        
        composable(AppRoutes.VENDER_PRODUCTO) {
            VenderProductoScreen()
        }
        
        composable(AppRoutes.CONFIG_APP) {
            ConfigAppScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
