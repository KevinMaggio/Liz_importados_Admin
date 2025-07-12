package com.refactoringlife.lizimportadosadmin.features.home.presenter.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.refactoringlife.lizimportadosadmin.features.home.presenter.viewmodel.HomeUiState
import com.refactoringlife.lizimportadosadmin.features.home.presenter.viewmodel.HomeViewModel
import com.refactoringlife.lizimportadosadmin.features.home.presenter.views.HomeDataView

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = HomeViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Launcher para seleccionar múltiples imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.processImages(context = androidx.compose.ui.platform.LocalContext.current, uris)
        }
    }
    
    // Manejar estados de la UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is HomeUiState.SelectingImages -> {
                imagePickerLauncher.launch("image/*")
            }
            else -> {}
        }
    }
    
    HomeDataView(
        modifier = modifier,
        uiState = uiState,
        onProcessImagesClick = {
            viewModel.startImageProcessing()
        }
    )
}