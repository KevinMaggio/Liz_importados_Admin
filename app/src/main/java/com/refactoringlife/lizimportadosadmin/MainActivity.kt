package com.refactoringlife.lizimportadosadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.refactoringlife.lizimportadosadmin.ui.theme.LizImportadosAdminTheme
import com.refactoringlife.lizimportadosadmin.core.navigator.AppNavHost
import com.refactoringlife.lizimportadosadmin.features.login.presenter.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by lazy { LoginViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LizImportadosAdminTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { padding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.navigationBarsPadding().padding(padding),
                        viewModel = loginViewModel
                    )
                }
            }
        }
    }
}