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
import com.refactoringlife.lizimportados.core.navigator.navigateFromLoginToHome
import com.refactoringlife.lizimportadosadmin.features.home.presenter.screens.HomeScreen
import com.refactoringlife.lizimportados.features.login.presenter.screens.LoginScreen
import com.refactoringlife.lizimportadosadmin.features.login.presenter.screens.LoginScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onGoogleSignInClick: (Intent) -> Unit = {}
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
                    navController.navigateFromLoginToHome()
                },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable(
            AppRoutes.HOME,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            HomeScreen(
                modifier = Modifier,
                navController
            )
        }
    }
}
