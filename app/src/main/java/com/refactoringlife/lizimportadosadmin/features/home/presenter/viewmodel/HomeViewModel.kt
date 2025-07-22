package com.refactoringlife.lizimportadosadmin.features.home.presenter.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refactoringlife.lizimportadosadmin.core.utils.ImageProcessor
import com.refactoringlife.lizimportadosadmin.core.utils.ImageOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class HomeViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "HomeViewModel"
    }
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _processedImages = MutableStateFlow<List<Uri>>(emptyList())
    val processedImages: StateFlow<List<Uri>> = _processedImages.asStateFlow()
    
    private val imageProcessor = ImageProcessor()
    
    /**
     * Inicia el proceso de selección de imágenes
     */
    fun startImageProcessing() {
        Log.d(TAG, "🖼️ Iniciando procesamiento de imágenes")
        _uiState.value = HomeUiState.SelectingImages
    }
    
    /**
     * Procesa las imágenes seleccionadas
     */
    suspend fun processImages(context: Context, imageUris: List<Uri>): List<Uri> {
        return imageUris.mapNotNull { uri ->
            imageProcessor.processImage(context, uri).getOrNull()?.uri
        }
    }
    
    /**
     * Resetea el estado
     */
    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
    
    /**
     * Limpia las imágenes procesadas
     */
    fun clearProcessedImages() {
        _processedImages.value = emptyList()
    }
}

/**
 * Estados de la UI para el Home
 */
sealed class HomeUiState {
    data object Idle : HomeUiState()
    data object SelectingImages : HomeUiState()
    data class ProcessingImages(val count: Int) : HomeUiState()
    data class ProcessingComplete(val processedCount: Int) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 