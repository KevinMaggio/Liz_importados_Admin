package com.refactoringlife.lizimportadosadmin.core.navigator

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
import com.refactoringlife.lizimportadosadmin.features.home.presenter.screens.HomeScreen
import com.refactoringlife.lizimportadosadmin.features.login.presenter.screens.LoginScreen
import com.refactoringlife.lizimportadosadmin.features.login.presenter.viewmodel.LoginViewModel
import com.refactoringlife.lizimportadosadmin.features.addProduct.presenter.screens.AddProductScreen
import com.refactoringlife.lizimportadosadmin.features.combo.presenter.screens.CreateComboScreen
import com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens.SelectProductForEditScreen
import com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens.EditProductDetailScreen
import com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens.DeleteProductScreen
import com.refactoringlife.lizimportadosadmin.features.editProduct.presenter.screens.VenderProductoScreen
import com.refactoringlife.lizimportadosadmin.features.home.presenter.screens.TestImageProcessingScreen

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onGoogleSignInClick: (Intent) -> Unit = {},
    viewModel: LoginViewModel
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN,
        modifier = modifier
    ) {
        composable(
            AppRoutes.LOGIN,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            LoginScreen(
                onGoogleClick = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoogleSignInClick = onGoogleSignInClick,
                viewModel = viewModel
            )
        }

        composable(
            AppRoutes.HOME,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            HomeScreen(navController = navController)
        }

        composable(
            "add_product",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            AddProductScreen()
        }

        composable(
            "create_combo",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            CreateComboScreen()
        }

        composable(
            "edit_product_select",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            SelectProductForEditScreen(onProductSelected = { productId ->
                navController.navigate("edit_product_detail/$productId")
            })
        }
        composable(
            "edit_product_detail/{productId}",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductDetailScreen(productId = productId, onBack = { navController.popBackStack() })
        }
        composable(
            "delete_product",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            DeleteProductScreen()
        }
        composable(
            "vender_producto",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            VenderProductoScreen()
        }
        composable(
            AppRoutes.TEST_IMAGE_PROCESSING,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            TestImageProcessingScreen()
        }
    }
}
