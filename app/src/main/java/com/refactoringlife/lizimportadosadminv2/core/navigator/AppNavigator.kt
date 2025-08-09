package com.refactoringlife.lizimportadosadminv2.core.navigator

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.refactoringlife.lizimportadosadminv2.features.login.presenter.screens.LoginScreen
import com.refactoringlife.lizimportadosadminv2.features.home.presenter.screens.HomeScreen
import com.refactoringlife.lizimportadosadminv2.features.addProduct.presenter.screens.AddProductScreen
import com.refactoringlife.lizimportadosadminv2.features.carts.presenter.screens.CartDetailScreen
import com.refactoringlife.lizimportadosadminv2.features.carts.presenter.screens.CartsScreen
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens.CreateComboScreen
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens.ManageCombosScreen
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.screens.SelectProductForComboScreen
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel.ComboViewModel
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.SelectProductForEditScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.EditProductDetailScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.DeleteProductScreen
import com.refactoringlife.lizimportadosadminv2.features.editProduct.presenter.screens.VenderProductoScreen
import com.refactoringlife.lizimportadosadminv2.features.config.presenter.screens.ConfigAppScreen
import com.refactoringlife.lizimportadosadminv2.features.login.presenter.viewmodel.LoginViewModel
import com.refactoringlife.lizimportadosadminv2.features.home.presenter.viewmodel.HomeViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: LoginViewModel,
    comboViewModel: ComboViewModel,
    onGoogleSignInClick: (android.content.Intent) -> Unit
) {
    NavHost(
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
                        viewModel = HomeViewModel(),
                        onNavigateToCarts = { navController.navigate(AppRoutes.CARTS) },
                        onNavigateToAddProduct = { navController.navigate(AppRoutes.ADD_PRODUCT) },
                        onNavigateToCreateCombo = { navController.navigate(AppRoutes.CREATE_COMBO) },
                        onNavigateToManageCombos = { navController.navigate(AppRoutes.MANAGE_COMBOS) },
                        onNavigateToSelectProductForEdit = { navController.navigate(AppRoutes.SELECT_PRODUCT_FOR_EDIT) },
                        onNavigateToEditProductDetail = { productId ->
                            navController.navigate("${AppRoutes.EDIT_PRODUCT_DETAIL}/$productId")
                        },
                        onNavigateToDeleteProduct = { navController.navigate(AppRoutes.DELETE_PRODUCT) },
                        onNavigateToVenderProducto = { navController.navigate(AppRoutes.VENDER_PRODUCTO) },
                        onNavigateToConfigApp = { navController.navigate(AppRoutes.CONFIG_APP) }
                    )
                }

                // Rutas de Carritos
                composable(AppRoutes.CARTS) {
                    CartsScreen(
                        onNavigateToCartDetail = { email ->
                            navController.navigate("${AppRoutes.CART_DETAIL}/$email")
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "${AppRoutes.CART_DETAIL}/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    CartDetailScreen(
                        email = email,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
        
        composable(AppRoutes.ADD_PRODUCT) {
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppRoutes.CREATE_COMBO) {
            CreateComboScreen(
                viewModel = comboViewModel,
                onNavigateToSelectProduct = { productNumber ->
                    navController.navigate("${AppRoutes.SELECT_PRODUCT_FOR_COMBO}/$productNumber")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppRoutes.MANAGE_COMBOS) {
            ManageCombosScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "${AppRoutes.SELECT_PRODUCT_FOR_COMBO}/{productNumber}"
        ) { backStackEntry ->
            val productNumber = backStackEntry.arguments?.getString("productNumber")?.toIntOrNull() ?: 1
            SelectProductForComboScreen(
                productNumber = productNumber,
                viewModel = comboViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppRoutes.SELECT_PRODUCT_FOR_EDIT) {
            SelectProductForEditScreen(
                onProductSelected = { productId ->
                    navController.navigate("${AppRoutes.EDIT_PRODUCT_DETAIL}/$productId")
                }
            )
        }
        
        composable(
            route = "${AppRoutes.EDIT_PRODUCT_DETAIL}/{productId}"
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
            VenderProductoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppRoutes.CONFIG_APP) {
            ConfigAppScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
