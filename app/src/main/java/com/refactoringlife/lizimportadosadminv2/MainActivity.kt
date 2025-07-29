package com.refactoringlife.lizimportadosadminv2

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
import com.refactoringlife.lizimportadosadminv2.ui.theme.LizImportadosAdminTheme
import com.refactoringlife.lizimportadosadminv2.core.navigator.AppNavHost
import com.refactoringlife.lizimportadosadminv2.features.login.presenter.viewmodel.LoginViewModel
import com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel.ComboViewModel
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val comboViewModel: ComboViewModel by viewModels()

    // Launcher para Google Sign-In
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            loginViewModel.handleSignInResult(data)
        }

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
                        viewModel = loginViewModel,
                        comboViewModel = comboViewModel,
                        onGoogleSignInClick = { intent ->
                            googleSignInLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}