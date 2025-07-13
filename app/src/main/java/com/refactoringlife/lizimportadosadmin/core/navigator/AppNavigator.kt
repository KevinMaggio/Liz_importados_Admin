package com.refactoringlife.lizimportadosadmin.core.navigator

import android.content.Intent
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
    }
}
