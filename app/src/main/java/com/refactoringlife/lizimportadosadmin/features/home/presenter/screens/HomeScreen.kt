package com.refactoringlife.lizimportadosadmin.features.home.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.refactoringlife.lizimportados.core.utils.getConfigMock
import com.refactoringlife.lizimportados.features.home.presenter.views.HomeDataView

@Composable
fun HomeScreen(
    modifier: Modifier,
    navController: NavHostController,
) {
    HomeDataView(
        modifier = modifier,
        configData = getConfigMock(),
        action = { _,_ -> }
    )
}