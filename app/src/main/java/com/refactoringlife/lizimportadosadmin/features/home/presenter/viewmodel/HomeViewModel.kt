package com.refactoringlife.lizimportadosadmin.features.home.presenter.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refactoringlife.lizimportadosadmin.core.utils.ImageProcessor
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
    fun processImages(context: Context, imageUris: List<Uri>) {
        Log.d(TAG, "🔄 Procesando ${imageUris.size} imágenes")
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.ProcessingImages(imageUris.size)
                
                // Procesar imágenes usando ImageProcessor
                val results = imageProcessor.processMultipleImages(context, imageUris)
                
                // Contar éxitos y errores
                val successfulCount = results.count { it.isSuccess }
                val errorCount = results.count { it.isFailure }
                
                if (errorCount > 0) {
                    Log.w(TAG, "⚠️ $errorCount imágenes fallaron al procesar")
                }
                
                Log.d(TAG, "✅ Procesamiento completado: $successfulCount exitosas, $errorCount fallidas")
                _uiState.value = HomeUiState.ProcessingComplete(successfulCount)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error procesando imágenes: ${e.message}")
                _uiState.value = HomeUiState.Error("Error procesando imágenes: ${e.message}")
            }
        }
    }
    
    /**
     * Resetea el estado
     */
    fun resetState() {
        _uiState.value = HomeUiState.Idle
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